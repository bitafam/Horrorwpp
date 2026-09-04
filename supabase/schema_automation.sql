-- ====================================================================
-- HORROR APP: AUTOMATION, CRON & EDGE FUNCTIONS SCHEMA
-- ====================================================================

-- 1. APP SETTINGS (ذخیره امن تنظیمات و کلید Gemini در دیتابیس)
CREATE TABLE IF NOT EXISTS public.app_settings (
    key TEXT PRIMARY KEY,
    value TEXT NOT NULL,
    description TEXT,
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- 2. AUTOMATION CONFIGS (پیکربندی زمان‌بندی و وظایف خودکار به صورت مستقل)
CREATE TABLE IF NOT EXISTS public.automation_configs (
    id TEXT PRIMARY KEY, -- 'AUTO_GRIM_FORTUNES', 'AUTO_SCENARIOS'
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    frequency TEXT NOT NULL DEFAULT 'DAILY' CHECK (frequency IN ('HOURLY', 'DAILY', 'TWICE_DAILY')),
    schedule_hour_1 INTEGER NOT NULL DEFAULT 0 CHECK (schedule_hour_1 BETWEEN 0 AND 23),
    schedule_minute_1 INTEGER NOT NULL DEFAULT 0 CHECK (schedule_minute_1 BETWEEN 0 AND 59),
    schedule_hour_2 INTEGER NOT NULL DEFAULT 12 CHECK (schedule_hour_2 BETWEEN 0 AND 23),
    schedule_minute_2 INTEGER NOT NULL DEFAULT 0 CHECK (schedule_minute_2 BETWEEN 0 AND 59),
    batch_count INTEGER NOT NULL DEFAULT 1 CHECK (batch_count BETWEEN 1 AND 50),
    custom_prompt TEXT,
    last_run_at TIMESTAMPTZ,
    next_run_at TIMESTAMPTZ,
    last_status TEXT,
    last_log TEXT,
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- Ensure minute columns exist if table was already created
ALTER TABLE public.automation_configs ADD COLUMN IF NOT EXISTS schedule_minute_1 INTEGER NOT NULL DEFAULT 0;
ALTER TABLE public.automation_configs ADD COLUMN IF NOT EXISTS schedule_minute_2 INTEGER NOT NULL DEFAULT 0;

-- 4. AUTOMATION LOGS (گزارش لاگ‌های اجرای فانکشن‌ها)
CREATE TABLE IF NOT EXISTS public.automation_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_type TEXT NOT NULL,
    status TEXT NOT NULL CHECK (status IN ('SUCCESS', 'FAILED', 'PENDING')),
    message TEXT NOT NULL,
    details JSONB,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- ====================================================================
-- SEED DATA & INITIAL CONFIGURATIONS
-- ====================================================================

-- ثبت رکوردهای پیش‌فرض تنظیمات
INSERT INTO public.app_settings (key, value, description)
VALUES 
    ('GEMINI_API_KEY', '', 'Google AI Studio Gemini API Key for Edge Functions'),
    ('GEMINI_MODEL', 'gemini-2.5-flash', 'Active Gemini Model for auto generations')
ON CONFLICT (key) DO NOTHING;

-- ثبت کانفیگ بخش‌ها (کاملاً ماژولار و مستقل)
INSERT INTO public.automation_configs (id, is_active, frequency, schedule_hour_1, schedule_hour_2, batch_count, custom_prompt)
VALUES 
    (
        'AUTO_GRIM_FORTUNES', 
        FALSE, 
        'DAILY', 
        0, -- ساعت ۱۲ شب هر ۲۴ ساعت
        0, 
        12, 
        'یک طالع‌بین تاریک و باستانی گوتیک شو و دقیقاً ۱۲ طالع شوم و دلهره‌آور، یکی برای هر ماه سال شمسی تولید کن.'
    ),
    (
        'AUTO_AI_STORIES', 
        TRUE, 
        'DAILY', 
        14, -- ساعت اول: ۱۴:۰۰
        22, -- ساعت دوم: ۲۲:۰۰
        3,  -- تعداد ۳ داستان در هر نوبت (تا ۲۰ داستان قابل تنظیم)
        'داستان‌های ترسناک، روانشناختی و ماورایی بسیار گیرا، با پایان‌های شوکه‌کننده و رازآلود بنویس.'
    )
ON CONFLICT (id) DO NOTHING;

-- ====================================================================
-- ROW LEVEL SECURITY (RLS) POLICIES
-- ====================================================================

ALTER TABLE public.app_settings ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.automation_configs ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.automation_logs ENABLE ROW LEVEL SECURITY;

-- 1. app_settings: همه دسترسی دارند (برای فانکشن‌ها و برنامه‌ساز)، ادمین مدیریت می‌کند
DROP POLICY IF EXISTS "Allow public read app_settings" ON public.app_settings;
CREATE POLICY "Allow public read app_settings" ON public.app_settings FOR SELECT USING (true);
DROP POLICY IF EXISTS "Allow all manage app_settings" ON public.app_settings;
CREATE POLICY "Allow all manage app_settings" ON public.app_settings FOR ALL USING (true) WITH CHECK (true);

-- 2. automation_configs: دسترسی خواندن و ویرایش برای ادمین و سیستم
DROP POLICY IF EXISTS "Allow all read automation_configs" ON public.automation_configs;
CREATE POLICY "Allow all read automation_configs" ON public.automation_configs FOR SELECT USING (true);
DROP POLICY IF EXISTS "Allow all manage automation_configs" ON public.automation_configs;
CREATE POLICY "Allow all manage automation_configs" ON public.automation_configs FOR ALL USING (true) WITH CHECK (true);

-- 4. automation_logs: دسترسی خواندن و درج لاگ
DROP POLICY IF EXISTS "Allow all read automation_logs" ON public.automation_logs;
CREATE POLICY "Allow all read automation_logs" ON public.automation_logs FOR SELECT USING (true);
DROP POLICY IF EXISTS "Allow all insert automation_logs" ON public.automation_logs;
CREATE POLICY "Allow all insert automation_logs" ON public.automation_logs FOR INSERT WITH CHECK (true);

-- ====================================================================
-- PG_CRON & SERVER-SIDE AUTOMATION PROCEDURES (اجرای ۱۰۰٪ ابری درون Supabase)
-- ====================================================================

-- ۱. تابع فراخوانی Edge Function از درون دیتابیس با استفاده از pg_net
CREATE OR REPLACE FUNCTION public.invoke_edge_function(function_name text, payload jsonb DEFAULT '{}'::jsonb)
RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    supabase_url text;
    anon_key text;
    service_key text;
    auth_header text;
    target_url text;
BEGIN
    SELECT value INTO supabase_url FROM public.app_settings WHERE key = 'SUPABASE_URL';
    SELECT value INTO service_key FROM public.app_settings WHERE key = 'SUPABASE_SERVICE_ROLE_KEY';
    SELECT value INTO anon_key FROM public.app_settings WHERE key = 'SUPABASE_ANON_KEY';
    
    auth_header := 'Bearer ' || COALESCE(NULLIF(service_key, ''), anon_key, '');
    
    IF supabase_url IS NOT NULL AND supabase_url <> '' THEN
        target_url := rtrim(supabase_url, '/') || '/functions/v1/' || function_name;
        
        PERFORM net.http_post(
            url := target_url,
            headers := jsonb_build_object(
                'Content-Type', 'application/json',
                'Authorization', auth_header,
                'apikey', COALESCE(NULLIF(anon_key, ''), service_key, '')
            ),
            body := payload
        );
    END IF;
EXCEPTION WHEN OTHERS THEN
    -- ثبت خطا در لاگ در صورت بروز مشکل در شبکه دیتابیس
    INSERT INTO public.automation_logs (task_type, status, message)
    VALUES ('INVOKE_' || upper(function_name), 'FAILED', 'خطا در فراخوانی دیتابیسی اج فانکشن: ' || SQLERRM);
END;
$$;

-- ۲. پروسیجر مستر زمان‌بندی سروری (بررسی دقیقه و ساعت تهران در دیتابیس)
CREATE OR REPLACE FUNCTION public.cron_run_automations()
RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    cur_tehran_hour integer;
    cur_tehran_minute integer;
    fortune_cfg record;
    scenario_cfg record;
BEGIN
    -- استخراج زمان دقیق تهران از ساعت سرور دیتابیس
    cur_tehran_hour := EXTRACT(HOUR FROM (now() AT TIME ZONE 'Asia/Tehran'))::integer;
    cur_tehran_minute := EXTRACT(MINUTE FROM (now() AT TIME ZONE 'Asia/Tehran'))::integer;

    -- بررسی اتوماسیون طالع تاریک
    SELECT * INTO fortune_cfg FROM public.automation_configs WHERE id = 'AUTO_GRIM_FORTUNES';
    IF fortune_cfg.is_active = TRUE THEN
        IF cur_tehran_hour = fortune_cfg.schedule_hour_1 AND cur_tehran_minute = fortune_cfg.schedule_minute_1 THEN
            PERFORM public.invoke_edge_function('auto-grim-fortunes', '{"cron": true}'::jsonb);
        END IF;
    END IF;

    -- بررسی اتوماسیون داستان‌های هوش مصنوعی
    SELECT * INTO story_cfg FROM public.automation_configs WHERE id = 'AUTO_AI_STORIES';
    IF story_cfg.is_active = TRUE THEN
        IF (cur_tehran_hour = story_cfg.schedule_hour_1 AND cur_tehran_minute = story_cfg.schedule_minute_1)
           OR (story_cfg.frequency = 'TWICE_DAILY' AND cur_tehran_hour = story_cfg.schedule_hour_2 AND cur_tehran_minute = story_cfg.schedule_minute_2) THEN
            PERFORM public.invoke_edge_function('auto-ai-stories', '{"cron": true}'::jsonb);
        END IF;
    END IF;
END;
$$;

-- ====================================================================
-- نحوه فعال‌سازی pg_cron در Supabase (کاملاً مستقل از گوشی):
-- ====================================================================
-- در بخش SQL Editor داشبورد Supabase کدهای زیر را اجرا کنید:
-- CREATE EXTENSION IF NOT EXISTS pg_cron;
-- CREATE EXTENSION IF NOT EXISTS pg_net;
-- 
-- اجرای هر دقیقه یکبار جاب برای تطابق سروری دقیق ساعت و دقیقه ایران:
-- SELECT cron.schedule(
--     'server-side-automation-runner',
--     '* * * * *',
--     $$ SELECT public.cron_run_automations(); $$
-- );

