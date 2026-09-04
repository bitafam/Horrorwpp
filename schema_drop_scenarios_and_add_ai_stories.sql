-- ====================================================================
-- HORROR APP: MIGRATION SCRIPT
-- حذف کامل سناریوهای بازی و جایگزینی با داستان‌های هوش مصنوعی
-- ====================================================================

-- ۱. حذف کامل جداول سناریو و روابط آن‌ها
DROP TABLE IF EXISTS public.wrong_choice_choices CASCADE;
DROP TABLE IF EXISTS public.wrong_choice_scenes CASCADE;
DROP TABLE IF EXISTS public.wrong_choice_scenarios CASCADE;

-- ۲. ساخت جدول داستان‌های هوش مصنوعی (AI Stories)
CREATE TABLE IF NOT EXISTS public.ai_stories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    genre TEXT DEFAULT 'روانشناختی',
    synopsis TEXT,
    cover_image_url TEXT,
    tags TEXT,
    status TEXT NOT NULL DEFAULT 'PUBLISHED' CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')),
    rating NUMERIC(3,1) DEFAULT 4.9,
    rating_count INTEGER DEFAULT 14,
    view_count INTEGER DEFAULT 192,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- ۳. تنظیم دسترسی‌ها (Row Level Security) برای داستان‌های هوش مصنوعی
ALTER TABLE public.ai_stories ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Allow public read ai_stories" ON public.ai_stories;
CREATE POLICY "Allow public read ai_stories" ON public.ai_stories 
    FOR SELECT USING (true);

DROP POLICY IF EXISTS "Allow all manage ai_stories" ON public.ai_stories;
CREATE POLICY "Allow all manage ai_stories" ON public.ai_stories 
    FOR ALL USING (true) WITH CHECK (true);

-- ۴. توابع شمارنده بازدید و امتیازدهی اتمیک
CREATE OR REPLACE FUNCTION public.increment_ai_story_view(story_id UUID)
RETURNS VOID AS $$
BEGIN
    UPDATE public.ai_stories
    SET view_count = COALESCE(view_count, 0) + 1
    WHERE id = story_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE OR REPLACE FUNCTION public.submit_ai_story_rating(story_id UUID, new_rating NUMERIC)
RETURNS VOID AS $$
BEGIN
    UPDATE public.ai_stories
    SET 
        rating_count = COALESCE(rating_count, 0) + 1,
        rating = ROUND(
            ((COALESCE(rating, 4.9) * COALESCE(rating_count, 0)) + new_rating) / (COALESCE(rating_count, 0) + 1),
            1
        )
    WHERE id = story_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- تریگر بروزرسانی خودکار زمان ویرایش
CREATE OR REPLACE TRIGGER trigger_ai_stories_updated_at 
    BEFORE UPDATE ON public.ai_stories 
    FOR EACH ROW EXECUTE FUNCTION update_modified_column();

-- ۵. بروزرسانی رکوردهای اتوماسیون (جایگزینی AUTO_SCENARIOS با AUTO_AI_STORIES)
DELETE FROM public.automation_configs WHERE id = 'AUTO_SCENARIOS';

INSERT INTO public.automation_configs (
    id, is_active, frequency, schedule_hour_1, schedule_minute_1, schedule_hour_2, schedule_minute_2, batch_count, custom_prompt
) VALUES (
    'AUTO_AI_STORIES', 
    TRUE, 
    'DAILY', 
    14, -- ساعت اول: ۱۴:۰۰ به وقت ایران
    0,
    22, -- ساعت دوم: ۲۲:۰۰ به وقت ایران
    0,
    3,  -- تعداد ۳ داستان در هر نوبت (تا ۲۰ داستان قابل تنظیم)
    'داستان‌های ترسناک، روانشناختی و ماورایی بسیار گیرا، با پایان‌های شوکه‌کننده و رازآلود بنویس.'
) ON CONFLICT (id) DO UPDATE SET
    custom_prompt = EXCLUDED.custom_prompt,
    batch_count = EXCLUDED.batch_count;

-- ۶. بروزرسانی پرامپت‌های پیش‌فرض
DELETE FROM public.ai_prompts WHERE prompt_key = 'WRONG_CHOICE_PROMPT';

INSERT INTO public.ai_prompts (prompt_key, prompt_text)
VALUES 
('AI_STORY_PROMPT', 'یک داستان ترسناک روانشناختی و ماورایی گوتیک بسیار جذاب با توصیفات هولناک خلق کن.')
ON CONFLICT (prompt_key) DO UPDATE SET prompt_text = EXCLUDED.prompt_text;

-- ۷. تابع هماهنگ‌کننده زمان‌بندی سروری ۱۰۰٪ ابری (بدون نیاز به گوشی ادمین)
CREATE OR REPLACE FUNCTION public.cron_run_automations()
RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    cur_tehran_hour integer;
    cur_tehran_minute integer;
    fortune_cfg record;
    story_cfg record;
BEGIN
    cur_tehran_hour := EXTRACT(HOUR FROM (now() AT TIME ZONE 'Asia/Tehran'))::integer;
    cur_tehran_minute := EXTRACT(MINUTE FROM (now() AT TIME ZONE 'Asia/Tehran'))::integer;

    -- ۱. بررسی اتوماسیون طالع شوم ۱۲ ماه
    SELECT * INTO fortune_cfg FROM public.automation_configs WHERE id = 'AUTO_GRIM_FORTUNES';
    IF fortune_cfg.is_active = TRUE THEN
        IF cur_tehran_hour = fortune_cfg.schedule_hour_1 AND cur_tehran_minute = fortune_cfg.schedule_minute_1 THEN
            PERFORM public.invoke_edge_function('auto-grim-fortunes', '{"cron": true}'::jsonb);
        END IF;
    END IF;

    -- ۲. بررسی اتوماسیون داستان‌های هوش مصنوعی
    SELECT * INTO story_cfg FROM public.automation_configs WHERE id = 'AUTO_AI_STORIES';
    IF story_cfg.is_active = TRUE THEN
        IF (cur_tehran_hour = story_cfg.schedule_hour_1 AND cur_tehran_minute = story_cfg.schedule_minute_1)
           OR (story_cfg.frequency = 'TWICE_DAILY' AND cur_tehran_hour = story_cfg.schedule_hour_2 AND cur_tehran_minute = story_cfg.schedule_minute_2) THEN
            PERFORM public.invoke_edge_function('auto-ai-stories', '{"cron": true}'::jsonb);
        END IF;
    END IF;
END;
$$;
