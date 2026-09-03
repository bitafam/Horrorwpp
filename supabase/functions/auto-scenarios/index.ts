// Supabase Edge Function: auto-scenarios
// Automatically generates and publishes deeply atmospheric, addictive horror scenarios using Gemini
// Separates narrative text from interactive choices in both database tables and structured format.

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

    // Check manual or cron triggers
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
        message: "اتوماسیون سناریوها غیرفعال است (is_active = false)."
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

    const hour1 = config?.schedule_hour_1 ?? 14;
    const min1 = config?.schedule_minute_1 ?? 0;
    const hour2 = config?.schedule_hour_2 ?? 22;
    const min2 = config?.schedule_minute_2 ?? 0;
    const freq = config?.frequency ?? "TWICE_DAILY";

    let shouldRunNow = isManualTrigger || isCronTrigger;
    if (!shouldRunNow && config?.is_active) {
      const matchSlot1 = currentTehranHour === hour1 && Math.abs(currentTehranMinute - min1) <= 2;
      const matchSlot2 = freq === "TWICE_DAILY" && currentTehranHour === hour2 && Math.abs(currentTehranMinute - min2) <= 2;
      if (matchSlot1 || matchSlot2) {
        shouldRunNow = true;
      }
    }

    if (!shouldRunNow) {
      return new Response(JSON.stringify({
        success: true,
        skipped: true,
        message: `ساعت فعلی ایران (${currentTehranHour}:${currentTehranMinute}) زمان نوبت انتشار نیست. اجرا نادیده گرفته شد.`
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

    // 3. Creative, Addictive Psychological Horror Prompt
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
      `تو استاد بزرگ تعلیق، وحشت گوتیک و بازی‌گردان سناریوهای بقای عمارت وحشت هستی.\n` +
      `وظیفه تو خلق سناریوهایی به شدت اعتیادآور، مرموز، هیجان‌انگیز و لذت‌بخش است که کاربر با خواندن هر انتخاب دچار ضربان قلب بالا و دلهره لذت‌بخش شود.\n\n` +
      `لطفاً دقیقاً تعداد ${batchCount} سناریوی کاملاً مجزا و اعتیادآور تولید کن.\n` +
      `قالب هر سناریو باید دقیقاً و بدون کم‌وکاست به شکل زیر باشد تا دیتابیس بتواند متن مطلب را از گزینه‌های قابل انتخاب تفکیک کند:\n\n` +
      `###سناریو###\n` +
      `عنوان: [نام جذاب، سینمایی و دلهره‌آور سناریو]\n` +
      `مطلب اصلی: [فضاسازی عمیق، توصیف صداها، تاریکی و ورود نفس‌گیر به این معمای شوم]\n\n` +
      `---مرحله ۱: [نام دلهره‌آور صحنه اول]\n` +
      `روایت: [شرح دقیق موقعیت مرگبار اول و معمای مقابل کاربر]\n` +
      `گزینه ۱: [متن انتخاب اول] -> [فرجام یا نتیجه پیشروی]\n` +
      `گزینه ۲: [متن انتخاب دوم] -> [تله شوم یا هلاکت / مرگ]\n` +
      `گزینه ۳: [متن انتخاب سوم] -> [مسیر پنهان یا بقا]\n\n` +
      `---مرحله ۲: [نام صحنه دوم]\n` +
      `روایت: [ادامه نفس‌گیر ماجرا و افزایش فشار روانی بر کاربر]\n` +
      `گزینه ۱: [متن انتخاب اول] -> [پیامد]\n` +
      `گزینه ۲: [متن انتخاب دوم] -> [مرگ]\n` +
      `گزینه ۳: [متن انتخاب سوم] -> [ادامه مسیر]\n\n` +
      `---مرحله ۳: [نام رویارویی نهایی]\n` +
      `روایت: [مواجهه پایانی با منشأ شرارت و آزمون سرنوشت روح]\n` +
      `گزینه ۱: [متن انتخاب نجات‌بخش] -> [شکستن طلسم و نجات کامل / پیروزی]\n` +
      `گزینه ۲: [متن انتخاب شوم] -> [اسارت ابدی روح در تاریکی / مرگ]`;

    // 4. Call Gemini REST API
    const geminiUrl = `https://generativelanguage.googleapis.com/v1beta/models/${modelName}:generateContent?key=${apiKey}`;
    const geminiResp = await fetch(geminiUrl, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        contents: [{ parts: [{ text: fullPrompt }] }],
        generationConfig: {
          temperature: 0.88,
          maxOutputTokens: 4500,
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
      blocks.push(candidateText.trim());
    }

    const createdScenarios = [];

    for (const block of blocks.slice(0, batchCount)) {
      const lines = block.split("\n").map((l: string) => l.trim()).filter(Boolean);
      let title = "سناریوی تاریک عمارت وحشت";
      let intro = "";

      for (const line of lines) {
        if (line.startsWith("عنوان:")) {
          title = line.replace("عنوان:", "").replace(/[#*]/g, "").trim();
        } else if (line.startsWith("مطلب اصلی:") || line.startsWith("مطلب:")) {
          intro = line.replace(/^(مطلب اصلی:|مطلب:)/, "").trim();
        }
      }

      // 1. Insert into wrong_choice_scenarios
      const { data: newScen, error: insertErr } = await supabase
        .from("wrong_choice_scenarios")
        .insert({
          title: title,
          description: block,
          status: "PUBLISHED",
        })
        .select()
        .single();

      if (insertErr || !newScen) {
        console.error("Error inserting scenario:", insertErr);
        continue;
      }

      // 2. Parse stages and choices to populate wrong_choice_scenes & wrong_choice_choices
      try {
        const stageSplits = block.split(/---مرحله\s*\d+[:\s-]*/);
        let firstSceneId: string | null = null;

        for (let sIdx = 1; sIdx < stageSplits.length; sIdx++) {
          const stageContent = stageSplits[sIdx].trim();
          if (!stageContent) continue;

          const stageLines = stageContent.split("\n").map(l => l.trim()).filter(Boolean);
          let narrativeText = "";
          const stageChoices: string[] = [];

          for (const sLine of stageLines) {
            if (sLine.startsWith("گزینه") || sLine.match(/^[0-9۰-۹]+[\.\-]/)) {
              stageChoices.push(sLine);
            } else if (!sLine.startsWith("عنوان") && !sLine.startsWith("---")) {
              const clean = sLine.replace(/^روایت:/, "").trim();
              if (clean) narrativeText += (narrativeText ? "\n" : "") + clean;
            }
          }

          if (!narrativeText && stageLines.length > 0) {
            narrativeText = stageLines[0];
          }

          const isEndingStage = sIdx === stageSplits.length - 1;

          // Insert scene
          const { data: sceneRow } = await supabase
            .from("wrong_choice_scenes")
            .insert({
              scenario_id: newScen.id,
              scene_text: narrativeText || `مرحله ${sIdx}`,
              is_ending: isEndingStage,
              ending_type: isEndingStage ? "CHOICE_DECIDED" : null
            })
            .select()
            .single();

          if (sceneRow) {
            if (!firstSceneId) firstSceneId = sceneRow.id;

            // Insert choices for this scene
            for (const cText of stageChoices) {
              await supabase
                .from("wrong_choice_choices")
                .insert({
                  scene_id: sceneRow.id,
                  choice_text: cText,
                });
            }
          }
        }

        // Link initial_scene_id if created
        if (firstSceneId) {
          await supabase
            .from("wrong_choice_scenarios")
            .update({ initial_scene_id: firstSceneId })
            .eq("id", newScen.id);
        }
      } catch (parseErr) {
        console.warn("Failed to parse scenes/choices into sub-tables:", parseErr);
      }

      createdScenarios.push(newScen);
    }

    if (createdScenarios.length === 0) {
      throw new Error("هیچ سناریویی ذخیره نشد.");
    }

    // 6. Log success
    const logMsg = `✅ اجرای خودکار: تعداد ${createdScenarios.length} سناریوی اعتیادآور و تفکیک‌شده با موفقیت در دیتابیس منتشر شد.`;
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
