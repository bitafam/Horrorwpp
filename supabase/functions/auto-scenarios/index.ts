// Supabase Edge Function: auto-scenarios
// Automatically generates and publishes interactive horror scenarios using Gemini

import { serve } from "https://deno.land/std@0.168.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2.39.0";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
};

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
      .eq("id", "AUTO_SCENARIOS")
      .single();

    // Check manual override
    let isManualTrigger = false;
    try {
      const body = await req.json().catch(() => ({}));
      if (body?.manual === true) isManualTrigger = true;
    } catch {
      // ignore
    }

    // Check if active
    if (!isManualTrigger && config && !config.is_active) {
      return new Response(JSON.stringify({
        success: true,
        message: "اتوماسیون سناریوها غیرفعال است (is_active = false)."
      }), {
        headers: { ...corsHeaders, "Content-Type": "application/json" }
      });
    }

    // Dynamic Iran/Tehran Hour Check
    const tehranTimeStr = new Intl.DateTimeFormat("en-US", {
      timeZone: "Asia/Tehran",
      hour: "numeric",
      hour12: false
    }).format(new Date());
    const currentTehranHour = parseInt(tehranTimeStr, 10);

    const hour1 = config?.schedule_hour_1 ?? 14;
    const hour2 = config?.schedule_hour_2 ?? 22;
    const freq = config?.frequency ?? "TWICE_DAILY";

    let shouldRunNow = isManualTrigger;
    if (!isManualTrigger && config?.is_active) {
      if (freq === "DAILY" && currentTehranHour === hour1) {
        shouldRunNow = true;
      } else if (freq === "TWICE_DAILY" && (currentTehranHour === hour1 || currentTehranHour === hour2)) {
        shouldRunNow = true;
      }
    }

    if (!shouldRunNow) {
      return new Response(JSON.stringify({
        success: true,
        skipped: true,
        message: `ساعت فعلی ایران (${currentTehranHour}:00) زمان نوبت انتشار (${freq === "DAILY" ? hour1 : `${hour1}, ${hour2}`}) نیست. اجرا نادیده گرفته شد.`
      }), {
        headers: { ...corsHeaders, "Content-Type": "application/json" }
      });
    }

    const batchCount = Math.max(1, Math.min(5, config?.batch_count || 1));

    // 2. Fetch Gemini API Key & Model
    const { data: settings } = await supabase
      .from("app_settings")
      .select("*");

    const dbApiKey = settings?.find((s: any) => s.key === "GEMINI_API_KEY")?.value;
    const dbModel = settings?.find((s: any) => s.key === "GEMINI_MODEL")?.value;

    const apiKey = dbApiKey || Deno.env.get("GEMINI_API_KEY");
    if (!apiKey) {
      throw new Error("کلید Gemini API Key یافت نشد. لطفاً در تنظیمات ادمین کلید را ثبت کنید.");
    }

    const modelName = dbModel || "gemini-2.5-flash";

    // 3. Fetch Prompt
    let promptBase = config?.custom_prompt;
    if (!promptBase) {
      const { data: promptRow } = await supabase
        .from("ai_prompts")
        .select("prompt_text")
        .eq("prompt_key", "WRONG_CHOICE_PROMPT")
        .single();
      promptBase = promptRow?.prompt_text || "یک سناریوی چند مرحله‌ای ترسناک گوتیک به همراه جزئیات برای بازی تعاملی بساز.";
    }

    const fullPrompt = `${promptBase}\n\n` +
      `لطفاً تعداد ${batchCount} سناریوی تعاملی ترسناک، دلهره‌آور و چند مرحله‌ای کاملاً مجزا بنویس.\n` +
      `برای هر سناریو:\n` +
      `- خط اول با 'عنوان: [نام سناریو]' آغاز شود.\n` +
      `- مراحل بازی، دوراهی‌ها، انتخاب‌های مرگ و بقا را شرح بده.\n` +
      `- هر سناریو را دقیقاً با '###سناریو###' از هم جدا کن.`;

    // 4. Call Gemini REST API
    const geminiUrl = `https://generativelanguage.googleapis.com/v1beta/models/${modelName}:generateContent?key=${apiKey}`;
    const geminiResp = await fetch(geminiUrl, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        contents: [{ parts: [{ text: fullPrompt }] }],
        generationConfig: {
          temperature: 0.85,
          maxOutputTokens: 4000,
        },
      }),
    });

    if (!geminiResp.ok) {
      const errText = await geminiResp.text();
      throw new Error(`خطای ارتباط با هوش مصنوعی (${geminiResp.status}): ${errText}`);
    }

    const geminiData = await geminiResp.json();
    const candidateText = geminiData.candidates?.[0]?.content?.parts?.[0]?.text ?? "";

    if (!candidateText) {
      throw new Error("پاسخی از هوش مصنوعی دریافت نشد.");
    }

    // 5. Split scenarios by marker
    const blocks = candidateText
      .split("###سناریو###")
      .map((b: string) => b.trim())
      .filter((b: string) => b.length > 30);

    if (blocks.length === 0) {
      // Fallback: entire text as single scenario
      blocks.push(candidateText.trim());
    }

    const createdScenarios = [];

    for (const block of blocks.slice(0, batchCount)) {
      const lines = block.split("\n").map((l: string) => l.trim()).filter(Boolean);
      let title = "سناریوی تاریک عمارت وحشت";
      if (lines.length > 0) {
        const firstLine = lines[0];
        if (firstLine.includes("عنوان:")) {
          title = firstLine.replace("عنوان:", "").replace(/[#*]/g, "").trim();
        }
      }

      const { data: newScen, error: insertErr } = await supabase
        .from("wrong_choice_scenarios")
        .insert({
          title: title,
          description: block,
          status: "PUBLISHED",
        })
        .select()
        .single();

      if (!insertErr && newScen) {
        createdScenarios.push(newScen);
      }
    }

    if (createdScenarios.length === 0) {
      throw new Error("هیچ سناریویی ذخیره نشد.");
    }

    // 6. Send in-app notification
    const firstTitle = createdScenarios[0]?.title || "سناریوی جدید";
    await supabase.from("app_notifications").insert({
      title: "🕯️ سناریوی انتخابی جدید منتشر شد",
      message: `سناریوی تعاملی «${firstTitle}» هم‌اکنون برای بازی آماده است. آیا زنده بیرون می‌آیید؟`,
      timestamp: Date.now(),
      status: "PUBLISHED",
      is_scheduled: false,
    });

    // 7. Log success
    const logMsg = `تعداد ${createdScenarios.length} سناریوی جدید با موفقیت تولید و منتشر شد.`;
    await supabase.from("automation_logs").insert({
      task_type: "AUTO_SCENARIOS",
      status: "SUCCESS",
      message: logMsg,
      details: {
        count: createdScenarios.length,
        titles: createdScenarios.map((s: any) => s.title),
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
      .eq("id", "AUTO_SCENARIOS");

    return new Response(
      JSON.stringify({
        success: true,
        message: logMsg,
        created: createdScenarios,
      }),
      {
        headers: { ...corsHeaders, "Content-Type": "application/json" },
        status: 200,
      }
    );
  } catch (error: any) {
    try {
      const supabaseUrl = Deno.env.get("SUPABASE_URL") ?? "";
      const supabaseServiceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? "";
      const supabase = createClient(supabaseUrl, supabaseServiceKey);
      await supabase.from("automation_logs").insert({
        task_type: "AUTO_SCENARIOS",
        status: "FAILED",
        message: `خطا در تولید خودکار سناریو: ${error.message}`,
      });
      await supabase
        .from("automation_configs")
        .update({
          last_run_at: new Date().toISOString(),
          last_status: "FAILED",
          last_log: error.message,
        })
        .eq("id", "AUTO_SCENARIOS");
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
