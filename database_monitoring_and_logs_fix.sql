-- ==============================================================================
-- عمارت وحشت: اسکریپت جامع تعمیر دیتابیس برای سیستم لاگ‌ها، مانیتورینگ و تل‌متری
-- Haunted House: Comprehensive Database Fix for Logs & Telemetry Monitoring
-- ==============================================================================
-- این اسکریپت تمامی جداول مورد نیاز برای بخش مانیتورینگ (کرش لاگ‌ها، کاربران آنلاین،
-- وضعیت اشتراک‌ها و لاگ‌های اتوماسیون) را ساخته و دسترسی‌های RLS را تنظیم می‌کند.
-- ==============================================================================

-- ۱. جدول لاگ‌های خطای برنامه (App Crash Logs)
CREATE TABLE IF NOT EXISTS public.app_crash_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    device_model TEXT,
    android_version TEXT,
    app_version TEXT,
    error_message TEXT NOT NULL,
    stack_trace TEXT,
    status TEXT NOT NULL DEFAULT 'UNRESOLVED', -- 'UNRESOLVED', 'RESOLVED'
    created_at TIMESTAMPTZ DEFAULT now()
);

-- افزودن ستون‌های احتمالی در صورت وجود قبلی جدول
ALTER TABLE public.app_crash_logs ADD COLUMN IF NOT EXISTS device_model TEXT;
ALTER TABLE public.app_crash_logs ADD COLUMN IF NOT EXISTS android_version TEXT;
ALTER TABLE public.app_crash_logs ADD COLUMN IF NOT EXISTS app_version TEXT;
ALTER TABLE public.app_crash_logs ADD COLUMN IF NOT EXISTS status TEXT DEFAULT 'UNRESOLVED';

-- تنظیم RLS جدول کرش لاگ‌ها
ALTER TABLE public.app_crash_logs ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Allow public insert app_crash_logs" ON public.app_crash_logs;
DROP POLICY IF EXISTS "Allow all manage app_crash_logs" ON public.app_crash_logs;
DROP POLICY IF EXISTS "Public full access app_crash_logs" ON public.app_crash_logs;

CREATE POLICY "Public full access app_crash_logs" 
ON public.app_crash_logs 
FOR ALL 
USING (true) 
WITH CHECK (true);


-- ۲. جدول ضربان آنلاین و وضعیت کاربران و مشترکین (User Heartbeats & Online Status)
CREATE TABLE IF NOT EXISTS public.user_heartbeats (
    device_id TEXT PRIMARY KEY,
    user_id TEXT,
    user_email TEXT,
    device_model TEXT,
    is_subscribed BOOLEAN DEFAULT false,
    subscription_plan TEXT,
    last_seen_at TIMESTAMPTZ DEFAULT now(),
    created_at TIMESTAMPTZ DEFAULT now()
);

-- افزودن ستون‌های جدید در صورت وجود قبلی جدول
ALTER TABLE public.user_heartbeats ADD COLUMN IF NOT EXISTS user_email TEXT;
ALTER TABLE public.user_heartbeats ADD COLUMN IF NOT EXISTS device_model TEXT;
ALTER TABLE public.user_heartbeats ADD COLUMN IF NOT EXISTS is_subscribed BOOLEAN DEFAULT false;
ALTER TABLE public.user_heartbeats ADD COLUMN IF NOT EXISTS subscription_plan TEXT;
ALTER TABLE public.user_heartbeats ADD COLUMN IF NOT EXISTS last_seen_at TIMESTAMPTZ DEFAULT now();

-- تنظیم RLS جدول ضربان کاربران
ALTER TABLE public.user_heartbeats ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Allow public upsert user_heartbeats" ON public.user_heartbeats;
DROP POLICY IF EXISTS "Allow all manage user_heartbeats" ON public.user_heartbeats;
DROP POLICY IF EXISTS "Public full access user_heartbeats" ON public.user_heartbeats;

CREATE POLICY "Public full access user_heartbeats" 
ON public.user_heartbeats 
FOR ALL 
USING (true) 
WITH CHECK (true);


-- ۳. جدول لاگ‌های اتوماسیون و تولید هوش مصنوعی (Automation Logs)
CREATE TABLE IF NOT EXISTS public.automation_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    section TEXT NOT NULL,
    provider TEXT NOT NULL,
    model TEXT NOT NULL,
    items_count INT DEFAULT 0,
    status TEXT NOT NULL,
    error_message TEXT,
    created_at TIMESTAMPTZ DEFAULT now()
);

ALTER TABLE public.automation_logs ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Public full access automation_logs" ON public.automation_logs;
CREATE POLICY "Public full access automation_logs" 
ON public.automation_logs 
FOR ALL 
USING (true) 
WITH CHECK (true);


-- ۴. جدول گزارش‌های تخلف محتوا (Story Reports)
CREATE TABLE IF NOT EXISTS public.story_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    story_id TEXT NOT NULL,
    story_title TEXT NOT NULL,
    story_author TEXT NOT NULL,
    story_type TEXT NOT NULL,
    reason TEXT NOT NULL,
    user_comment TEXT,
    created_at TIMESTAMPTZ DEFAULT now()
);

ALTER TABLE public.story_reports ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Public full access story_reports" ON public.story_reports;
CREATE POLICY "Public full access story_reports" 
ON public.story_reports 
FOR ALL 
USING (true) 
WITH CHECK (true);


-- ۵. جدول تنظیمات سراسری اپلیکیشن (App Settings)
CREATE TABLE IF NOT EXISTS public.app_settings (
    key TEXT PRIMARY KEY,
    value TEXT NOT NULL,
    description TEXT,
    updated_at TIMESTAMPTZ DEFAULT now()
);

ALTER TABLE public.app_settings ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Public full access app_settings" ON public.app_settings;
CREATE POLICY "Public full access app_settings" 
ON public.app_settings 
FOR ALL 
USING (true) 
WITH CHECK (true);


-- ۶. اصلاح جدول profiles جهت پیشگیری از خطای ستون‌های اشتراک
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'profiles') THEN
        ALTER TABLE public.profiles ADD COLUMN IF NOT EXISTS subscription_tier TEXT DEFAULT 'FREE';
        ALTER TABLE public.profiles ADD COLUMN IF NOT EXISTS is_subscribed BOOLEAN DEFAULT false;
    END IF;
END $$;


-- ۷. اعطای دسترسی‌های لازم به تمامی نقش‌ها (anon, authenticated, service_role)
GRANT USAGE ON SCHEMA public TO anon, authenticated, service_role;
GRANT ALL ON ALL TABLES IN SCHEMA public TO anon, authenticated, service_role;
GRANT ALL ON ALL SEQUENCES IN SCHEMA public TO anon, authenticated, service_role;
GRANT ALL ON ALL ROUTINES IN SCHEMA public TO anon, authenticated, service_role;

ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO anon, authenticated, service_role;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO anon, authenticated, service_role;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON ROUTINES TO anon, authenticated, service_role;

-- پایان اسکریپت با موفقیت
SELECT 'Database logs and telemetry tables configured successfully!' AS result;
