// Supabase Edge Function: auto-grim-fortunes
// Automatically generates and updates 12 Grim Fortunes every 24h using Gemini

import { serve } from "https://deno.land/std@0.168.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2.39.0";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
};

const PERSIAN_MONTHS = [
  "فروردین", "اردیبهشت", "خرداد",
  "تیر", "مرداد", "شهریور",
  "مهر", "آبان", "آذر",
  "دی", "بهمن", "اسفند"
];

serve(async (req) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  try {
    const supabaseUrl = Deno.env.get("SUPABASE_URL") ?? "";
    const supabaseServiceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? "";
    const supabase = createClient(supabaseUrl, supabaseServiceKey);

    // 1. Fetch config
    const { data: config } = await supabase
      .from("automation_configs")
      .select("*")
      .eq("id", "AUTO_GRIM_FORTUNES")
      .single();

    // Check manual override or database cron trigger
    let isManualTrigger = false;
    let isCronTrigger = false;
    try {
      const body = await req.json().catch(() => ({}));
      if (body?.manual === true) isManualTrigger = true;
      if (body?.cron === true) isCronTrigger = true;
    } catch {
      // ignore
    }

    // Check if active
    if (!isManualTrigger && !isCronTrigger && config && !config.is_active) {
      return new Response(JSON.stringify({
        success: true,
        message: "اتوماسیون طالع شوم غیرفعال است (is_active = false)."
      }), {
        headers: { ...corsHeaders, "Content-Type": "application/json" }
      });
    }

    // Dynamic Iran/Tehran Hour & Minute Check
    const tehranTimeStr = new Intl.DateTimeFormat("en-US", {
      timeZone: "Asia/Tehran",
      hour: "numeric",
      minute: "numeric",
      hour12: false
    }).format(new Date());

    const [hourStr, minStr] = tehranTimeStr.split(":");
    const currentTehranHour = parseInt(hourStr, 10);
    const currentTehranMinute = parseInt(minStr, 10);

    const targetHour = config?.schedule_hour_1 ?? 0;
    const targetMinute = config?.schedule_minute_1 ?? 0;

    let shouldRunNow = isManualTrigger || isCronTrigger;
    if (!shouldRunNow && config?.is_active) {
      if (currentTehranHour === targetHour && Math.abs(currentTehranMinute - targetMinute) <= 2) {
        shouldRunNow = true;
      }
    }

    if (!shouldRunNow) {
      return new Response(JSON.stringify({
        success: true,
        skipped: true,
        message: `ساعت فعلی ایران (${currentTehranHour}:${currentTehranMinute}) با زمان تنظیم شده در پنل (${targetHour}:${targetMinute}) مطابقت ندارد. اجرا نادیده گرفته شد.`
      }), {
        headers: { ...corsHeaders, "Content-Type": "application/json" }
      });
    }

    // 2. Fetch Gemini API Key & Model from app_settings
    const { data: settings } = await supabase
      .from("app_settings")
      .select("*");

    const dbApiKey = settings?.find((s: any) => s.key === "GEMINI_API_KEY")?.value;
    const dbModel = settings?.find((s: any) => s.key === "GEMINI_MODEL")?.value;

    const apiKey = dbApiKey || Deno.env.get("GEMINI_API_KEY");
    if (!apiKey) {
      throw new Error("کلید Gemini API Key نه در جدول app_settings و نه در Secrets یافت نشد. لطفاً در پنل ادمین کلید را ذخیره کنید.");
    }

    const rawModel = dbModel || "gemini-2.5-flash";
    const modelName = rawModel.includes("3.5") ? "gemini-2.5-flash" : rawModel;

    // 3. Fetch Prompt
    let promptBase = config?.custom_prompt;
    if (!promptBase) {
      const { data: promptRow } = await supabase
        .from("ai_prompts")
        .select("prompt_text")
        .eq("prompt_key", "GRIM_FORTUNE_PROMPT")
        .single();
      promptBase = promptRow?.prompt_text || "یک طالع‌بین تاریک و باستانی گوتیک شو و دقیقاً ۱۲ طالع شوم و دلهره‌آور، یکی برای هر ماه سال شمسی تولید کن.";
    }

    const fullPrompt = `${promptBase}\n\n` +
      `پاسخ خود را دقیقاً و صرفاً به صورت یک ساختار معتبر JSON فارسی با ساختار زیر بازگردان. هیچ توضیح اضافی قبل یا بعد از JSON ارائه نده:\n` +
      `{\n` +
      `  "fortunes": [\n` +
      `    {\n` +
      `      "month_index": 1,\n` +
      `      "month_name": "فروردین",\n` +
      `      "title": "یک عنوان حماسی و شوم ترسناک",\n` +
      `      "fortune_text": "تفسیر طالع و هشدار تاریک و پیش‌گویی هولناک مخصوص متولدین این ماه",\n` +
      `      "omen_poem": "یک تک‌بیت شعر فال تاریک ملهم از حافظ شیرازی",\n` +
      `      "doom_level": "بسیار شوم"\n` +
      `    }\n` +
      `  ]\n` +
      `}\n` +
      `باید برای هر ۱۲ ماه سال (فروردین تا اسفند) یعنی month_index های ۱ تا ۱۲ دقیقاً یک آیتم وجود داشته باشد.`;

    // 4. Call Gemini REST API
    const geminiUrl = `https://generativelanguage.googleapis.com/v1beta/models/${modelName}:generateContent?key=${apiKey}`;
    const geminiResp = await fetch(geminiUrl, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        contents: [{ parts: [{ text: fullPrompt }] }],
        generationConfig: {
          temperature: 0.8,
          maxOutputTokens: 3000,
        },
      }),
    });

    if (!geminiResp.ok) {
      const errText = await geminiResp.text();
      throw new Error(`خطای تماس با Gemini API (${geminiResp.status}): ${errText}`);
    }

    const geminiData = await geminiResp.json();
    const candidateText = geminiData.candidates?.[0]?.content?.parts?.[0]?.text ?? "";

    if (!candidateText) {
      throw new Error("پاسخی از مدل هوش مصنوعی دریافت نشد.");
    }

    // 5. Parse JSON from response
    const startIndex = candidateText.indexOf("{");
    const endIndex = candidateText.lastIndexOf("}");
    if (startIndex === -1 || endIndex === -1) {
      throw new Error("پاسخ هوش مصنوعی فاقد ساختار معتبر JSON است: " + candidateText.slice(0, 100));
    }

    const jsonStr = candidateText.substring(startIndex, endIndex + 1);
    const parsed = JSON.parse(jsonStr);
    const fortunes = parsed.fortunes || [];

    if (!Array.isArray(fortunes) || fortunes.length === 0) {
      throw new Error("لیست طالع‌ها در خروجی هوش مصنوعی خالی است.");
    }

    // 6. Upsert fortunes into grim_fortunes table
    const recordsToUpsert = fortunes.map((f: any) => {
      const mIndex = Number(f.month_index);
      return {
        month_index: mIndex,
        month_name: f.month_name || PERSIAN_MONTHS[mIndex - 1] || `ماه ${mIndex}`,
        title: f.title || `طالع ماه ${mIndex}`,
        fortune_text: f.fortune_text || "سرنوشت شوم در انتظار است...",
        omen_poem: f.omen_poem || null,
        doom_level: f.doom_level || "شوم",
        status: "PUBLISHED",
        updated_at: new Date().toISOString(),
      };
    });

    const { error: upsertErr } = await supabase
      .from("grim_fortunes")
      .upsert(recordsToUpsert, { onConflict: "month_index" });

    if (upsertErr) {
      throw new Error(`خطا در ذخیره‌سازی طالع‌ها در پایگاه‌داده: ${upsertErr.message}`);
    }

    // 7. Log success
    const logMsg = `تعداد ${recordsToUpsert.length} طالع شوم با مدل ${modelName} با موفقیت تولید و منتشر شد.`;
    await supabase.from("automation_logs").insert({
      task_type: "AUTO_GRIM_FORTUNES",
      status: "SUCCESS",
      message: logMsg,
      details: {
        model: modelName,
        count: recordsToUpsert.length,
        generated_at: new Date().toISOString(),
      },
    });

    await supabase
      .from("automation_configs")
      .update({
        last_run_at: new Date().toISOString(),
        last_status: "SUCCESS",
        last_log: logMsg,
      })
      .eq("id", "AUTO_GRIM_FORTUNES");

    return new Response(
      JSON.stringify({
        success: true,
        message: logMsg,
        count: recordsToUpsert.length,
      }),
      {
        headers: { ...corsHeaders, "Content-Type": "application/json" },
        status: 200,
      }
    );
  } catch (error: any) {
    // Log failure
    try {
      const supabaseUrl = Deno.env.get("SUPABASE_URL") ?? "";
      const supabaseServiceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? "";
      const supabase = createClient(supabaseUrl, supabaseServiceKey);
      await supabase.from("automation_logs").insert({
        task_type: "AUTO_GRIM_FORTUNES",
        status: "FAILED",
        message: `خطا در تولید خودکار طالع: ${error.message}`,
      });
      await supabase
        .from("automation_configs")
        .update({
          last_run_at: new Date().toISOString(),
          last_status: "FAILED",
          last_log: error.message,
        })
        .eq("id", "AUTO_GRIM_FORTUNES");
    } catch (_) {}

    return new Response(
      JSON.stringify({ success: false, error: error.message }),
      {
        headers: { ...corsHeaders, "Content-Type": "application/json" },
        status: 500,
      }
    );
  }
});
