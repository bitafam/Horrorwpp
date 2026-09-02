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

-- 2. APP NOTIFICATIONS (با قابلیت زمان‌بندی و وضعیت انتشار)
CREATE TABLE IF NOT EXISTS public.app_notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title TEXT NOT NULL,
    message TEXT NOT NULL,
    image_url TEXT,
    timestamp BIGINT NOT NULL,
    is_scheduled BOOLEAN DEFAULT FALSE,
    scheduled_at BIGINT, -- زمان میلادی به میلی‌ثانیه برای انتشار
    status TEXT NOT NULL DEFAULT 'PUBLISHED' CHECK (status IN ('PUBLISHED', 'PENDING_SCHEDULE', 'CANCELLED')),
    created_at TIMESTAMPTZ DEFAULT now()
);

-- 3. AUTOMATION CONFIGS (پیکربندی زمان‌بندی و وظایف خودکار به صورت مستقل)
CREATE TABLE IF NOT EXISTS public.automation_configs (
    id TEXT PRIMARY KEY, -- 'SCHEDULED_NOTIFICATIONS', 'AUTO_GRIM_FORTUNES', 'AUTO_SCENARIOS'
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    frequency TEXT NOT NULL DEFAULT 'DAILY' CHECK (frequency IN ('HOURLY', 'DAILY', 'TWICE_DAILY')),
    schedule_hour_1 INTEGER NOT NULL DEFAULT 0 CHECK (schedule_hour_1 BETWEEN 0 AND 23),
    schedule_hour_2 INTEGER NOT NULL DEFAULT 12 CHECK (schedule_hour_2 BETWEEN 0 AND 23),
    batch_count INTEGER NOT NULL DEFAULT 1 CHECK (batch_count BETWEEN 1 AND 50),
    custom_prompt TEXT,
    last_run_at TIMESTAMPTZ,
    next_run_at TIMESTAMPTZ,
    last_status TEXT,
    last_log TEXT,
    updated_at TIMESTAMPTZ DEFAULT now()
);

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

-- ثبت کانفیگ ۳ بخش مجزا (کاملاً ماژولار و مستقل)
INSERT INTO public.automation_configs (id, is_active, frequency, schedule_hour_1, schedule_hour_2, batch_count, custom_prompt)
VALUES 
    (
        'SCHEDULED_NOTIFICATIONS', 
        TRUE, 
        'HOURLY', 
        0, 
        0, 
        1, 
        'بررسی و انتشار اعلان‌های زمان‌بندی‌شده سر موعد'
    ),
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
        'AUTO_SCENARIOS', 
        FALSE, 
        'TWICE_DAILY', 
        14, -- ساعت اول: ۱۴:۰۰
        22, -- ساعت دوم: ۲۲:۰۰
        1,  -- تعداد ۱ سناریو در هر نوبت
        'یک سناریوی چند مرحله‌ای ترسناک گوتیک به همراه جزئیات برای بازی تعاملی بساز.'
    )
ON CONFLICT (id) DO NOTHING;

-- ====================================================================
-- ROW LEVEL SECURITY (RLS) POLICIES
-- ====================================================================

ALTER TABLE public.app_settings ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.app_notifications ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.automation_configs ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.automation_logs ENABLE ROW LEVEL SECURITY;

-- 1. app_settings: همه دسترسی دارند (برای فانکشن‌ها و برنامه‌ساز)، ادمین مدیریت می‌کند
DROP POLICY IF EXISTS "Allow public read app_settings" ON public.app_settings;
CREATE POLICY "Allow public read app_settings" ON public.app_settings FOR SELECT USING (true);
DROP POLICY IF EXISTS "Allow all manage app_settings" ON public.app_settings;
CREATE POLICY "Allow all manage app_settings" ON public.app_settings FOR ALL USING (true) WITH CHECK (true);

-- 2. app_notifications: همه اعلان‌های منتشر شده را می‌خوانند، ادمین مدیریت می‌کند
DROP POLICY IF EXISTS "Allow public read published notifications" ON public.app_notifications;
CREATE POLICY "Allow public read published notifications" ON public.app_notifications 
    FOR SELECT USING (status = 'PUBLISHED' OR is_scheduled = false);
DROP POLICY IF EXISTS "Allow all manage notifications" ON public.app_notifications;
CREATE POLICY "Allow all manage notifications" ON public.app_notifications FOR ALL USING (true) WITH CHECK (true);

-- 3. automation_configs: دسترسی خواندن و ویرایش برای ادمین و سیستم
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
-- PG_CRON SETUP (اجرای زمان‌بندی‌شده درون دیتابیس Supabase)
-- ====================================================================
-- برای فعال‌سازی در پنل Supabase > Database > Extensions اکستنشن‌های pg_cron و pg_net را فعال کنید:
-- CREATE EXTENSION IF NOT EXISTS pg_cron;
-- CREATE EXTENSION IF NOT EXISTS pg_net;

/*
-- مثال زمان‌بندی اجرای Edge Function ها با pg_cron:

-- ۱. اجرای بررسی اعلان‌های زمان‌بندی‌شده (هر ۵ دقیقه یک‌بار):
SELECT cron.schedule(
    'cron-check-scheduled-notifications',
    '*/5 * * * *',
    $$
    SELECT net.http_post(
        url := 'https://<PROJECT_REF>.supabase.co/functions/v1/scheduled-notifications',
        headers := '{"Content-Type": "application/json", "Authorization": "Bearer <SERVICE_ROLE_KEY>"}'::jsonb,
        body := '{}'::jsonb
    );
    $$
);

-- ۲. اجرای تولید روزانه طالع تاریک (هر روز ساعت ۰۰:۰۰ بامداد):
SELECT cron.schedule(
    'cron-auto-grim-fortunes-daily',
    '0 0 * * *',
    $$
    SELECT net.http_post(
        url := 'https://<PROJECT_REF>.supabase.co/functions/v1/auto-grim-fortunes',
        headers := '{"Content-Type": "application/json", "Authorization": "Bearer <SERVICE_ROLE_KEY>"}'::jsonb,
        body := '{}'::jsonb
    );
    $$
);

-- ۳. اجرای تولید سناریوهای ترسناک (۲ بار در روز - ساعت ۱۴ و ۲۲):
SELECT cron.schedule(
    'cron-auto-scenarios-slot-1',
    '0 14 * * *',
    $$
    SELECT net.http_post(
        url := 'https://<PROJECT_REF>.supabase.co/functions/v1/auto-scenarios',
        headers := '{"Content-Type": "application/json", "Authorization": "Bearer <SERVICE_ROLE_KEY>"}'::jsonb,
        body := '{}'::jsonb
    );
    $$
);

SELECT cron.schedule(
    'cron-auto-scenarios-slot-2',
    '0 22 * * *',
    $$
    SELECT net.http_post(
        url := 'https://<PROJECT_REF>.supabase.co/functions/v1/auto-scenarios',
        headers := '{"Content-Type": "application/json", "Authorization": "Bearer <SERVICE_ROLE_KEY>"}'::jsonb,
        body := '{}'::jsonb
    );
    $$
);
*/
