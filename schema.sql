-- ==========================================
-- HORRORAPP DATABASE SCHEMA MIGRATION
-- ==========================================

-- Enable pgcrypto extension for gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- 1. PROFILES TABLE (Syncs with auth.users)
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    email TEXT,
    role TEXT NOT NULL DEFAULT 'USER' CHECK (role IN ('USER', 'ADMIN')),
    created_at TIMESTAMPTZ DEFAULT now()
);

-- 2. GRIM FORTUNES (طالع‌های شوم)
CREATE TABLE IF NOT EXISTS public.grim_fortunes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    month_index INTEGER NOT NULL UNIQUE CHECK (month_index BETWEEN 1 AND 12),
    month_name TEXT,
    title TEXT NOT NULL,
    fortune_text TEXT NOT NULL,
    omen_poem TEXT,
    doom_level TEXT DEFAULT 'شوم',
    status TEXT NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')),
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- 3. REAL STORIES (داستان‌های واقعی)
CREATE TABLE IF NOT EXISTS public.real_stories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    author TEXT,
    source TEXT,
    cover_image_url TEXT,
    tags TEXT,
    status TEXT NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')),
    rating NUMERIC(3,1) DEFAULT 4.8,
    rating_count INTEGER DEFAULT 18,
    view_count INTEGER DEFAULT 340,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- 4. WRONG CHOICE SCENARIOS (سناریوهای انتخاب اشتباه)
CREATE TABLE IF NOT EXISTS public.wrong_choice_scenarios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title TEXT NOT NULL,
    description TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')),
    initial_scene_id UUID,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- 5. WRONG CHOICE SCENES (صحنه‌ها)
CREATE TABLE IF NOT EXISTS public.wrong_choice_scenes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    scenario_id UUID NOT NULL REFERENCES public.wrong_choice_scenarios(id) ON DELETE CASCADE,
    scene_text TEXT NOT NULL,
    is_ending BOOLEAN DEFAULT FALSE,
    ending_type TEXT CHECK (ending_type IN ('SURVIVED', 'DEAD', 'MYSTERY', 'SECRET', NULL)),
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- Bind initial_scene_id FK on scenario table
ALTER TABLE public.wrong_choice_scenarios 
    ADD CONSTRAINT fk_initial_scene 
    FOREIGN KEY (initial_scene_id) 
    REFERENCES public.wrong_choice_scenes(id) ON DELETE SET NULL;

-- 6. WRONG CHOICE CHOICES (انتخاب‌ها)
CREATE TABLE IF NOT EXISTS public.wrong_choice_choices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    scene_id UUID NOT NULL REFERENCES public.wrong_choice_scenes(id) ON DELETE CASCADE,
    choice_text TEXT NOT NULL,
    next_scene_id UUID REFERENCES public.wrong_choice_scenes(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- 7. USER STORY SUBMISSIONS (ارسالی‌های کاربران)
CREATE TABLE IF NOT EXISTS public.user_story_submissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    author_name TEXT NOT NULL,
    cover_image_url TEXT,
    tags TEXT,
    status TEXT NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PUBLISHED', 'REJECTED')),
    admin_notes TEXT,
    rating NUMERIC(3,2) DEFAULT 5.0,
    rating_count INTEGER DEFAULT 1,
    view_count INTEGER DEFAULT 1,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- 8. AI PROMPTS
CREATE TABLE IF NOT EXISTS public.ai_prompts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    prompt_key TEXT UNIQUE NOT NULL,
    prompt_text TEXT NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- 9. AI PROVIDERS
CREATE TABLE IF NOT EXISTS public.ai_providers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    provider_name TEXT NOT NULL,
    model_name TEXT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- ==========================================
-- DEFAULT METADATA PROVISIONING
-- ==========================================

INSERT INTO public.ai_prompts (prompt_key, prompt_text)
VALUES 
('GRIM_FORTUNE_PROMPT', 'یک طالع‌بین تاریک و باستانی گوتیک شو و دقیقاً ۱۲ طالع شوم و دلهره‌آور، یکی برای هر ماه سال شمسی تولید کن.'),
('WRONG_CHOICE_PROMPT', 'یک سناریوی چند مرحله‌ای ترسناک گوتیک به همراه جزئیات برای بازی تعاملی بساز.')
ON CONFLICT (prompt_key) DO UPDATE SET prompt_text = EXCLUDED.prompt_text;

INSERT INTO public.ai_providers (provider_name, model_name, is_active)
VALUES 
('Gemini', 'gemini-3.5-flash', TRUE),
('Gemini', 'gemini-1.5-pro', TRUE)
ON CONFLICT DO NOTHING;

-- ==========================================
-- UPDATED_AT TRIGGER FUNCTION
-- ==========================================

CREATE OR REPLACE FUNCTION update_modified_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Bind updated_at trigger to all tables
CREATE OR REPLACE TRIGGER trigger_grim_fortunes_updated_at BEFORE UPDATE ON public.grim_fortunes FOR EACH ROW EXECUTE FUNCTION update_modified_column();
CREATE OR REPLACE TRIGGER trigger_real_stories_updated_at BEFORE UPDATE ON public.real_stories FOR EACH ROW EXECUTE FUNCTION update_modified_column();
CREATE OR REPLACE TRIGGER trigger_wrong_choice_scenarios_updated_at BEFORE UPDATE ON public.wrong_choice_scenarios FOR EACH ROW EXECUTE FUNCTION update_modified_column();
CREATE OR REPLACE TRIGGER trigger_wrong_choice_scenes_updated_at BEFORE UPDATE ON public.wrong_choice_scenes FOR EACH ROW EXECUTE FUNCTION update_modified_column();
CREATE OR REPLACE TRIGGER trigger_wrong_choice_choices_updated_at BEFORE UPDATE ON public.wrong_choice_choices FOR EACH ROW EXECUTE FUNCTION update_modified_column();
CREATE OR REPLACE TRIGGER trigger_user_story_submissions_updated_at BEFORE UPDATE ON public.user_story_submissions FOR EACH ROW EXECUTE FUNCTION update_modified_column();
CREATE OR REPLACE TRIGGER trigger_ai_prompts_updated_at BEFORE UPDATE ON public.ai_prompts FOR EACH ROW EXECUTE FUNCTION update_modified_column();
CREATE OR REPLACE TRIGGER trigger_ai_providers_updated_at BEFORE UPDATE ON public.ai_providers FOR EACH ROW EXECUTE FUNCTION update_modified_column();

-- ==========================================
-- AUTOMATIC PROFILE CREATION TRIGGER
-- ==========================================

CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.profiles (id, email, role)
    VALUES (NEW.id, NEW.email, 'USER')
    ON CONFLICT (id) DO NOTHING;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE OR REPLACE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- ==========================================
-- ATOMIC RPC PROCEDURES
-- ==========================================

-- 1. Increment Story View Count
CREATE OR REPLACE FUNCTION public.increment_story_view(story_id UUID)
RETURNS VOID AS $$
BEGIN
    UPDATE public.real_stories
    SET view_count = COALESCE(view_count, 0) + 1
    WHERE id = story_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 2. Submit Story Rating Count
CREATE OR REPLACE FUNCTION public.submit_story_rating(story_id UUID, new_rating NUMERIC)
RETURNS VOID AS $$
BEGIN
    UPDATE public.real_stories
    SET 
        rating_count = COALESCE(rating_count, 0) + 1,
        rating = ROUND(
            ((COALESCE(rating, 4.8) * COALESCE(rating_count, 0)) + new_rating) / (COALESCE(rating_count, 0) + 1),
            1
        )
    WHERE id = story_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ==========================================
-- ROW-LEVEL SECURITY (RLS) & ACCESS CONTROL
-- ==========================================

-- Helper function to check if auth.uid() is Admin
CREATE OR REPLACE FUNCTION public.is_admin()
RETURNS BOOLEAN AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 FROM public.profiles
        WHERE id = auth.uid() AND role = 'ADMIN'
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Enable RLS on all tables
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.grim_fortunes ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.real_stories ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.wrong_choice_scenarios ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.wrong_choice_scenes ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.wrong_choice_choices ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_story_submissions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.ai_prompts ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.ai_providers ENABLE ROW LEVEL SECURITY;

-- 1. profiles Policies
CREATE POLICY "Allow public select on profiles" ON public.profiles FOR SELECT USING (true);
CREATE POLICY "Allow individual update on own profile" ON public.profiles FOR UPDATE USING (auth.uid() = id);
CREATE POLICY "Allow admin full manage on profiles" ON public.profiles ALL USING (public.is_admin());

-- 2. grim_fortunes Policies
CREATE POLICY "Allow public read published fortunes" ON public.grim_fortunes FOR SELECT USING (status = 'PUBLISHED' OR public.is_admin());
CREATE POLICY "Allow admin manage fortunes" ON public.grim_fortunes ALL USING (public.is_admin());

-- 3. real_stories Policies
CREATE POLICY "Allow public read published stories" ON public.real_stories FOR SELECT USING (status = 'PUBLISHED' OR public.is_admin());
CREATE POLICY "Allow admin manage stories" ON public.real_stories ALL USING (public.is_admin());

-- 4. wrong_choice_scenarios Policies
CREATE POLICY "Allow public read published scenarios" ON public.wrong_choice_scenarios FOR SELECT USING (status = 'PUBLISHED' OR public.is_admin());
CREATE POLICY "Allow admin manage scenarios" ON public.wrong_choice_scenarios ALL USING (public.is_admin());

-- 5. wrong_choice_scenes Policies
CREATE POLICY "Allow public read scenes" ON public.wrong_choice_scenes FOR SELECT USING (true);
CREATE POLICY "Allow admin manage scenes" ON public.wrong_choice_scenes ALL USING (public.is_admin());

-- 6. wrong_choice_choices Policies
CREATE POLICY "Allow public read choices" ON public.wrong_choice_choices FOR SELECT USING (true);
CREATE POLICY "Allow admin manage choices" ON public.wrong_choice_choices ALL USING (public.is_admin());

-- 7. user_story_submissions Policies
CREATE POLICY "Allow anyone to insert user stories" ON public.user_story_submissions FOR INSERT WITH CHECK (true);
CREATE POLICY "Allow public read published user stories" ON public.user_story_submissions FOR SELECT USING (status = 'PUBLISHED' OR public.is_admin());
CREATE POLICY "Allow admin full manage submissions" ON public.user_story_submissions ALL USING (public.is_admin());

-- 8. ai_prompts Policies
CREATE POLICY "Allow public read ai_prompts" ON public.ai_prompts FOR SELECT USING (true);
CREATE POLICY "Allow admin manage ai_prompts" ON public.ai_prompts ALL USING (public.is_admin());

-- 9. ai_providers Policies
CREATE POLICY "Allow public read ai_providers" ON public.ai_providers FOR SELECT USING (true);
CREATE POLICY "Allow admin manage ai_providers" ON public.ai_providers ALL USING (public.is_admin());

-- ==========================================
-- ATOMIC RPC FUNCTIONS FOR VIEWS & RATINGS
-- ==========================================

-- 1. Increment View for Real Story
CREATE OR REPLACE FUNCTION public.increment_story_view(story_id UUID)
RETURNS void AS $$
BEGIN
    UPDATE public.real_stories
    SET view_count = COALESCE(view_count, 0) + 1
    WHERE id = story_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 2. Submit Rating for Real Story
CREATE OR REPLACE FUNCTION public.submit_story_rating(story_id UUID, new_rating NUMERIC)
RETURNS void AS $$
DECLARE
    current_rating NUMERIC;
    current_count INTEGER;
    updated_rating NUMERIC;
BEGIN
    SELECT COALESCE(rating, 0), COALESCE(rating_count, 0)
    INTO current_rating, current_count
    FROM public.real_stories
    WHERE id = story_id;

    IF FOUND THEN
        updated_rating := ROUND(((current_rating * current_count + new_rating) / (current_count + 1)), 2);
        UPDATE public.real_stories
        SET rating = updated_rating,
            rating_count = current_count + 1
        WHERE id = story_id;
    END IF;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 3. Increment View for User Story Submission
CREATE OR REPLACE FUNCTION public.increment_submission_view(submission_id UUID)
RETURNS void AS $$
BEGIN
    UPDATE public.user_story_submissions
    SET view_count = COALESCE(view_count, 0) + 1
    WHERE id = submission_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 4. Submit Rating for User Story Submission
CREATE OR REPLACE FUNCTION public.submit_submission_rating(submission_id UUID, new_rating NUMERIC)
RETURNS void AS $$
DECLARE
    current_rating NUMERIC;
    current_count INTEGER;
    updated_rating NUMERIC;
BEGIN
    SELECT COALESCE(rating, 0), COALESCE(rating_count, 0)
    INTO current_rating, current_count
    FROM public.user_story_submissions
    WHERE id = submission_id;

    IF FOUND THEN
        updated_rating := ROUND(((current_rating * current_count + new_rating) / (current_count + 1)), 2);
        UPDATE public.user_story_submissions
        SET rating = updated_rating,
            rating_count = current_count + 1
        WHERE id = submission_id;
    END IF;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Grant execution permissions
GRANT EXECUTE ON FUNCTION public.increment_story_view(UUID) TO anon, authenticated;
GRANT EXECUTE ON FUNCTION public.submit_story_rating(UUID, NUMERIC) TO anon, authenticated;
GRANT EXECUTE ON FUNCTION public.increment_submission_view(UUID) TO anon, authenticated;
GRANT EXECUTE ON FUNCTION public.submit_submission_rating(UUID, NUMERIC) TO anon, authenticated;

-- ====================================================================
-- AUTOMATION, CRON & EDGE FUNCTIONS SCHEMA
-- ====================================================================

CREATE TABLE IF NOT EXISTS public.app_settings (
    key TEXT PRIMARY KEY,
    value TEXT NOT NULL,
    description TEXT,
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS public.app_notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title TEXT NOT NULL,
    message TEXT NOT NULL,
    image_url TEXT,
    timestamp BIGINT NOT NULL,
    is_scheduled BOOLEAN DEFAULT FALSE,
    scheduled_at BIGINT,
    status TEXT NOT NULL DEFAULT 'PUBLISHED' CHECK (status IN ('PUBLISHED', 'PENDING_SCHEDULE', 'CANCELLED')),
    trigger_condition TEXT,
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS public.automation_configs (
    id TEXT PRIMARY KEY,
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    frequency TEXT NOT NULL DEFAULT 'DAILY' CHECK (frequency IN ('HOURLY', 'DAILY', 'TWICE_DAILY')),
    schedule_hour_1 INTEGER NOT NULL DEFAULT 0 CHECK (schedule_hour_1 BETWEEN 0 AND 23),
    schedule_hour_2 INTEGER NOT NULL DEFAULT 12 CHECK (schedule_hour_2 BETWEEN 0 AND 23),
    batch_count INTEGER NOT NULL DEFAULT 1 CHECK (batch_count BETWEEN 1 AND 10),
    custom_prompt TEXT,
    last_run_at TIMESTAMPTZ,
    next_run_at TIMESTAMPTZ,
    last_status TEXT,
    last_log TEXT,
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS public.automation_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_type TEXT NOT NULL,
    status TEXT NOT NULL CHECK (status IN ('SUCCESS', 'FAILED', 'PENDING')),
    message TEXT NOT NULL,
    details JSONB,
    created_at TIMESTAMPTZ DEFAULT now()
);

INSERT INTO public.app_settings (key, value, description)
VALUES 
    ('GEMINI_API_KEY', '', 'Google AI Studio Gemini API Key for Edge Functions'),
    ('GEMINI_MODEL', 'gemini-2.5-flash', 'Active Gemini Model for auto generations')
ON CONFLICT (key) DO NOTHING;

INSERT INTO public.automation_configs (id, is_active, frequency, schedule_hour_1, schedule_hour_2, batch_count, custom_prompt)
VALUES 
    ('SCHEDULED_NOTIFICATIONS', TRUE, 'HOURLY', 0, 0, 1, 'بررسی و انتشار اعلان‌های زمان‌بندی‌شده سر موعد'),
    ('AUTO_GRIM_FORTUNES', FALSE, 'DAILY', 0, 0, 12, 'یک طالع‌بین تاریک و باستانی گوتیک شو و دقیقاً ۱۲ طالع شوم و دلهره‌آور، یکی برای هر ماه سال شمسی تولید کن.'),
    ('AUTO_SCENARIOS', FALSE, 'TWICE_DAILY', 14, 22, 1, 'یک سناریوی چند مرحله‌ای ترسناک گوتیک به همراه جزئیات برای بازی تعاملی بساز.')
ON CONFLICT (id) DO NOTHING;

ALTER TABLE public.app_settings ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.app_notifications ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.automation_configs ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.automation_logs ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Allow public read app_settings" ON public.app_settings FOR SELECT USING (true);
CREATE POLICY "Allow all manage app_settings" ON public.app_settings ALL USING (true);

CREATE POLICY "Allow public read published notifications" ON public.app_notifications FOR SELECT USING (status = 'PUBLISHED' OR is_scheduled = false);
CREATE POLICY "Allow all manage notifications" ON public.app_notifications FOR ALL USING (true);

CREATE POLICY "Allow all read automation_configs" ON public.automation_configs FOR SELECT USING (true);
CREATE POLICY "Allow all manage automation_configs" ON public.automation_configs ALL USING (true);

CREATE POLICY "Allow all read automation_logs" ON public.automation_logs FOR SELECT USING (true);
CREATE POLICY "Allow all insert automation_logs" ON public.automation_logs FOR INSERT WITH CHECK (true);

-- STORY REPORTS TABLE FOR USER COMPLIANCE
CREATE TABLE IF NOT EXISTS public.story_reports (
    id TEXT PRIMARY KEY,
    story_id TEXT NOT NULL,
    story_title TEXT NOT NULL,
    story_author TEXT NOT NULL,
    story_type TEXT NOT NULL,
    reason TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now()
);

ALTER TABLE public.story_reports ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Allow public insert story_reports" ON public.story_reports FOR INSERT WITH CHECK (true);
CREATE POLICY "Allow all manage story_reports" ON public.story_reports FOR ALL USING (true);

