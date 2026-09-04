// Supabase Edge Function: auto-ai-stories
// Automatically generates and publishes deeply atmospheric, Gothic Horror AI stories using Gemini
// Stores them in public.ai_stories table as 'PUBLISHED' with random posters and rich metadata.

import { serve } from "https://deno.land/std@0.168.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2.39.0";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
};

const POSTER_PRESETS = [
  "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?q=80&w=800&auto=format&fit=crop",
  "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?q=80&w=800&auto=format&fit=crop",
  "https://images.unsplash.com/photo-1508739773434-c26b3d09e071?q=80&w=800&auto=format&fit=crop",
  "https://images.unsplash.com/photo-1519074069444-1ba4fff16def?q=80&w=800&auto=format&fit=crop",
  "https://images.unsplash.com/photo-1514565131-fce0801e5785?q=80&w=800&auto=format&fit=crop",
  "https://images.unsplash.com/photo-1542281286-9e0a16bb7366?q=80&w=800&auto=format&fit=crop",
  "https://images.unsplash.com/photo-1516589178581-6cd7833ae3b2?q=80&w=800&auto=format&fit=crop",
  "https://images.unsplash.com/photo-1478760329108-5c3ed9d495a0?q=80&w=800&auto=format&fit=crop",
  "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?q=80&w=800&auto=format&fit=crop",
  "https://images.unsplash.com/photo-1518709766631-a6a7f45921c3?q=80&w=800&auto=format&fit=crop",
  "https://images.unsplash.com/photo-1509248961158-e54f6934749c?q=80&w=800&auto=format&fit=crop",
  "https://images.unsplash.com/photo-1579783902614-a3fb3927b675?q=80&w=800&auto=format&fit=crop"
];

serve(async (req) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  try {
    const supabaseUrl = Deno.env.get("SUPABASE_URL") ?? "";
    const supabaseServiceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? "";
    const supabase = createClient(supabaseUrl, supabaseServiceKey);

    // 1. Fetch config for AUTO_AI_STORIES
    const { data: config } = await supabase
      .from("automation_configs")
      .select("*")
      .eq("id", "AUTO_AI_STORIES")
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
        message: "اتوماسیون داستان‌های هوش مصنوعی غیرفعال است (is_active = false)."
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
    const freq = config?.frequency ?? "DAILY";

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

    const batchCount = Math.max(1, Math.min(20, config?.batch_count || 3));

    // 2. Fetch Gemini API Key & Model
    const { data: settings } = await supabase
      .from("app_settings")
      .select("*");

    const dbApiKey = settings?.find((s: any) => s.key === "GEMINI_API_KEY")?.value;
    const dbModel = settings?.find((s: any) => s.key === "GEMINI_MODEL")?.value;

    const apiKey = dbApiKey || Deno.env.get("GEMINI_API_KEY");
    if (!apiKey) {
      throw new Error("کلید Gemini API Key یافت نشد. لطفاً در تنظیمات پنل ادمین کلید را ثبت کنید.");
    }

    const modelName = (dbModel && dbModel.includes("gemini")) ? dbModel : "gemini-2.5-flash";

    // 3. Creative Gothic Horror Prompt
    let promptBase = config?.custom_prompt;
    if (!promptBase) {
      const { data: promptRow } = await supabase
        .from("ai_prompts")
        .select("prompt_text")
        .eq("prompt_key", "AI_STORY_PROMPT")
        .single();
      promptBase = promptRow?.prompt_text || "داستان‌های ترسناک، روانشناختی و ماورایی بسیار گیرا، با پایان‌های شوکه‌کننده و رازآلود بنویس.";
    }

    const fullPrompt = `${promptBase}\n\n` +
      `تو کاتب باستانی و استاد تعلیق و وحشت گوتیک عمارت وحشت هستی.\n` +
      `لطفاً دقیقاً تعداد ${batchCount} داستان کاملاً مجزا، عمیق، دلهره‌آور و اعتیادآور به زبان فارسی تولید کن.\n` +
      `پاسخ خود را صرفاً به شکل یک JSON معتبر فارسی بازگردان و هیچ توضیح اضافی خارج از JSON ننویس:\n` +
      `{\n` +
      `  "stories": [\n` +
      `    {\n` +
      `      "title": "یک عنوان جذاب و هولناک",\n` +
      `      "genre": "روانشناختی / ماورایی / جنایی / جن و ارواح / هیولایی / گوتیک / افسانه ایرانی",\n` +
      `      "synopsis": "خلاصه دو جمله‌ای دلهره‌آور از داستان",\n` +
      `      "content": "متن کامل، جذاب و چند پاراگرافی داستان با پایان غافلگیرکننده و شوکه‌کننده",\n` +
      `      "tags": "وحشت, ماورایی, رازآلود"\n` +
      `    }\n` +
      `  ]\n` +
      `}`;

    // 4. Call Google Gemini API
    const geminiUrl = `https://generativelanguage.googleapis.com/v1beta/models/${modelName}:generateContent?key=${apiKey}`;
    const geminiRes = await fetch(geminiUrl, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        contents: [{
          parts: [{ text: fullPrompt }]
        }],
        systemInstruction: {
          parts: [{ text: "تو کاتب باستانی و راوی وحشت عمارت هستی. فقط به زبان فارسی فصیح، ادبی و گوتیک پاسخ بده. خروجی فقط JSON معتبر باشد." }]
        },
        generationConfig: {
          temperature: 0.8,
          topP: 0.95,
          maxOutputTokens: 8192
        }
      })
    });

    if (!geminiRes.ok) {
      const errText = await geminiRes.text();
      throw new Error(`خطای Gemini API (${geminiRes.status}): ${errText}`);
    }

    const geminiData = await geminiRes.json();
    const candidateText = geminiData.candidates?.[0]?.content?.parts?.[0]?.text ?? "";

    // 5. Parse JSON
    const jsonStart = candidateText.indexOf("{");
    const jsonEnd = candidateText.lastIndexOf("}");
    if (jsonStart === -1 || jsonEnd === -1) {
      throw new Error("پاسخ هوش مصنوعی فرمت JSON ندارد.");
    }

    const jsonStr = candidateText.substring(jsonStart, jsonEnd + 1);
    const parsedData = JSON.parse(jsonStr);
    const rawStories = parsedData.stories || [];

    if (!Array.isArray(rawStories) || rawStories.length === 0) {
      throw new Error("هیچ داستانی در پاسخ تولید نشد.");
    }

    // 6. Insert into public.ai_stories
    const insertedStories: any[] = [];
    for (let i = 0; i < rawStories.length; i++) {
      const s = rawStories[i];
      if (!s.title || !s.content) continue;

      const randomPoster = POSTER_PRESETS[Math.floor(Math.random() * POSTER_PRESETS.length)];
      const randomRating = (4.7 + Math.random() * 0.3).toFixed(1);
      const randomViews = Math.floor(100 + Math.random() * 300);

      const storyObj = {
        title: s.title.trim(),
        content: s.content.trim(),
        genre: s.genre?.trim() || "روانشناختی",
        synopsis: s.synopsis?.trim() || s.content.substring(0, 100),
        cover_image_url: randomPoster,
        tags: s.tags?.trim() || "هوش مصنوعی, وحشت",
        status: "PUBLISHED", // Newly generated stories are placed into Published
        rating: parseFloat(randomRating),
        rating_count: Math.floor(10 + Math.random() * 20),
        view_count: randomViews
      };

      const { data, error } = await supabase
        .from("ai_stories")
        .insert(storyObj)
        .select()
        .single();

      if (!error && data) {
        insertedStories.push(data);
      }
    }

    // 7. Update automation_configs & log
    const nowIso = new Date().toISOString();
    const successMsg = `تعداد ${insertedStories.length} داستان هوش مصنوعی با مدل ${modelName} با موفقیت تولید و در لیست منتشر شده ثبت گردید.`;

    await supabase
      .from("automation_configs")
      .update({
        last_run_at: nowIso,
        last_status: "SUCCESS",
        last_log: successMsg,
        updated_at: nowIso
      })
      .eq("id", "AUTO_AI_STORIES");

    await supabase
      .from("automation_logs")
      .insert({
        task_type: "AUTO_AI_STORIES",
        status: "SUCCESS",
        message: successMsg,
        details: { count: insertedStories.length, model: modelName }
      });

    return new Response(JSON.stringify({
      success: true,
      message: successMsg,
      stories: insertedStories
    }), {
      headers: { ...corsHeaders, "Content-Type": "application/json" }
    });

  } catch (err: any) {
    const errorMsg = err.message || String(err);
    try {
      const supabaseUrl = Deno.env.get("SUPABASE_URL") ?? "";
      const supabaseServiceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? "";
      if (supabaseUrl && supabaseServiceKey) {
        const supabase = createClient(supabaseUrl, supabaseServiceKey);
        await supabase
          .from("automation_logs")
          .insert({
            task_type: "AUTO_AI_STORIES",
            status: "FAILED",
            message: `خطا در اجرای خودکار داستان‌ها: ${errorMsg}`
          });

        await supabase
          .from("automation_configs")
          .update({
            last_run_at: new Date().toISOString(),
            last_status: "FAILED",
            last_log: errorMsg
          })
          .eq("id", "AUTO_AI_STORIES");
      }
    } catch {
      // ignore secondary error
    }

    return new Response(JSON.stringify({
      success: false,
      error: errorMsg
    }), {
      status: 500,
      headers: { ...corsHeaders, "Content-Type": "application/json" }
    });
  }
});
