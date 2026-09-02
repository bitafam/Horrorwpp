// Supabase Edge Function: scheduled-notifications
// Triggered every 5-15 minutes or via cron to publish due notifications

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

    const now = Date.now();

    // 1. Fetch pending scheduled notifications whose scheduled_at is past or now
    const { data: pendingNotifications, error: fetchErr } = await supabase
      .from("app_notifications")
      .select("*")
      .eq("is_scheduled", true)
      .eq("status", "PENDING_SCHEDULE")
      .lte("scheduled_at", now);

    if (fetchErr) {
      throw new Error(`Database error fetching scheduled notifications: ${fetchErr.message}`);
    }

    let publishedCount = 0;
    if (pendingNotifications && pendingNotifications.length > 0) {
      for (const notif of pendingNotifications) {
        const { error: updateErr } = await supabase
          .from("app_notifications")
          .update({
            status: "PUBLISHED",
            timestamp: now,
          })
          .eq("id", notif.id);

        if (!updateErr) {
          publishedCount++;
        }
      }
    }

    // 2. Log result in automation_logs
    await supabase.from("automation_logs").insert({
      task_type: "SCHEDULED_NOTIFICATIONS",
      status: "SUCCESS",
      message: `بررسی اعلان‌های زمان‌بندی‌شده انجام شد. تعداد ${publishedCount} اعلان منتشر گردید.`,
      details: {
        checked_at: new Date().toISOString(),
        published_count: publishedCount,
        pending_total: pendingNotifications ? pendingNotifications.length : 0,
      },
    });

    // 3. Update automation_configs last_run
    await supabase
      .from("automation_configs")
      .update({
        last_run_at: new Date().toISOString(),
        last_status: "SUCCESS",
        last_log: `تعداد ${publishedCount} اعلان منتشر شد.`,
      })
      .eq("id", "SCHEDULED_NOTIFICATIONS");

    return new Response(
      JSON.stringify({
        success: true,
        message: `تعداد ${publishedCount} اعلان زمان‌بندی‌شده با موفقیت فعال و منتشر شدند.`,
        publishedCount,
      }),
      {
        headers: { ...corsHeaders, "Content-Type": "application/json" },
        status: 200,
      }
    );
  } catch (error: any) {
    return new Response(
      JSON.stringify({ success: false, error: error.message }),
      {
        headers: { ...corsHeaders, "Content-Type": "application/json" },
        status: 500,
      }
    );
  }
});
