/**
 * Gemini AI Client for HorrorHouse Web Admin
 * Exactly matches Android HorrorViewModel AI capabilities & models.
 */

const SUPPORTED_GEMINI_MODELS = [
    { id: "gemini-2.5-flash", desc: "مدل 2.5 Flash - جدیدترین و قدرتمندترین مدل پیش‌فرض هوش تاریکی" },
    { id: "gemini-2.0-flash", desc: "مدل 2.0 Flash - بسیار پرسرعت و بهینه‌شده برای تولید محتوا و طالع" },
    { id: "gemini-1.5-flash", desc: "مدل 1.5 Flash - مدل پایدار، سریع و سازگار با کلیدهای رایگان" },
    { id: "gemini-1.5-pro", desc: "مدل 1.5 Pro - با بالاترین عمق سناریونویسی و جزئیات ادبی" }
];

const DEFAULT_AI_STORY_PROMPT = `وظیفه تو تولید یک داستان ترسناک کاملاً تخیلی، اورجینال، منسجم و سینمایی است که مخاطب را از اولین پاراگراف تا آخرین جمله درگیر نگه دارد.

━━━━━━━━━━━━━━━━━━━━
قوانین نگارش
━━━━━━━━━━━━━━━━━━━━
1. داستان باید دارای ایده اصلی نوآورانه و شخصیت‌پردازی باورپذیر باشد.
2. شدت ترس باید به‌تدریج افزایش یابد و در نقطه اوج به اوج برسد.
3. از عناصر فولکلور ایرانی، فضاهای بومی و فضاسازی سینمایی استفاده کن.
4. پایان داستان باید هوشمندانه، منطقی و اثرگذار باشد.

━━━━━━━━━━━━━━━━━━━━
فرمت خروجی — بسیار مهم
━━━━━━━━━━━━━━━━━━━━
خروجی نهایی باید فقط و فقط یک JSON معتبر باشد. هیچ متن، توضیح یا Markdown اضافه قبل یا بعد از JSON قرار نده.

ساختار دقیق خروجی:
{
  "title": "عنوان داستان",
  "genre": "ژانر دقیق داستان",
  "content": "متن کامل داستان (حدود ۱۵۰۰ تا ۲۵۰۰ کلمه)",
  "synopsis": "خلاصه داستان در ۲ تا ۳ جمله کنجکاوی‌برانگیز"
}`;

const DEFAULT_GRIM_FORTUNE_PROMPT = `تو کاهن اعظم و پیشگوی تاریکی عمارت وحشت هستی. برای تمام ۱۲ ماه سال خورشیدی (فروردین تا اسفند) یک طالع شوم و دلهره‌آور بر اساس ادبیات گوتیک و اشعار تاریک بنویس.
برای هر ماه خروجی را دقیقاً با این فرمت جدا کن:
===1===
عنوان: [عنوان کوتاه و شوم طالع]
شعر: [یک بیت شعر شوم و گوتیک سبک حافظ]
طالع: [تفسیر سرنوشت، هشدار ماورایی و طالع شوم متولدین این ماه]
درجه: [شوم / بسیار شوم / نفرین ابدی]

و به همین ترتیب تا ===12=== برای اسفند ادامه بده.`;

const GeminiService = {
    getApiKey() {
        return localStorage.getItem('HORROR_GEMINI_KEY') || '';
    },
    setApiKey(key) {
        localStorage.setItem('HORROR_GEMINI_KEY', (key || '').trim());
    },
    async resolveApiKey() {
        let key = this.getApiKey();
        if (key) return key;
        // Try fetching from Supabase app_settings table
        if (window.SupabaseService && window.SupabaseService.isConfigured()) {
            try {
                const setting = await window.SupabaseService.getSetting('GEMINI_API_KEY');
                if (setting) {
                    this.setApiKey(setting);
                    return setting;
                }
            } catch (e) {
                console.warn('Could not fetch GEMINI_API_KEY from database:', e);
            }
        }
        return '';
    },
    getModel() {
        let model = localStorage.getItem('HORROR_GEMINI_MODEL') || 'gemini-2.5-flash';
        if (model.includes('3.') || !model.startsWith('gemini-')) {
            model = 'gemini-2.5-flash';
            localStorage.setItem('HORROR_GEMINI_MODEL', model);
        }
        return model;
    },
    setModel(model) {
        localStorage.setItem('HORROR_GEMINI_MODEL', model);
    },

    async testModel(apiKey, modelName) {
        const key = (apiKey || await this.resolveApiKey()).trim();
        if (!key) throw new Error('کلید API وارد نشده است و در دیتابیس نیز یافت نشد.');
        const model = modelName || this.getModel();

        const url = `https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent?key=${key}`;
        const response = await fetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                contents: [{ parts: [{ text: "آیا ارتباط شما با عمارت وحشت برقرار است؟ فقط یک کلمه پاسخ بده: «بله، متصل است»" }] }]
            })
        });

        if (!response.ok) {
            const err = await response.text();
            throw new Error(`خطای وضعیت ${response.status}: ${err}`);
        }

        const data = await response.json();
        const reply = data.candidates?.[0]?.content?.parts?.[0]?.text;
        return reply ? `ارتباط با مدل ${model} موفقیت‌آمیز بود: ${reply}` : `پاسخی دریافت نشد.`;
    },

    async generateStory(genre, customInstruction, blacklistTitles = [], customPrompt = null) {
        const key = await this.resolveApiKey();
        if (!key) throw new Error('کلید Gemini API در تب «تنظیم AI» یا دیتابیس یافت نشد.');
        const model = this.getModel();

        const basePrompt = customPrompt || localStorage.getItem('HORROR_AI_STORY_PROMPT') || DEFAULT_AI_STORY_PROMPT;
        
        let promptText = `${basePrompt}\n\nژانر مورد نظر: ${genre || 'ماورایی'}`;
        if (customInstruction) {
            promptText += `\nدستور ویژه: ${customInstruction}`;
        }
        if (blacklistTitles && blacklistTitles.length > 0) {
            promptText += `\nعناوین زیر را هرگز تکرار نکن:\n${blacklistTitles.slice(0, 30).join('، ')}`;
        }

        const url = `https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent?key=${key}`;
        const response = await fetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                contents: [{ parts: [{ text: promptText }] }],
                generationConfig: {
                    temperature: 0.85,
                    maxOutputTokens: 8192,
                    responseMimeType: 'application/json'
                }
            })
        });

        if (!response.ok) {
            const err = await response.text();
            throw new Error(`خطای Gemini (${response.status}): ${err}`);
        }

        const data = await response.json();
        const text = data.candidates?.[0]?.content?.parts?.[0]?.text;
        if (!text) throw new Error('متنی توسط هوش مصنوعی تولید نشد.');

        try {
            const cleaned = text.replace(/```json/g, '').replace(/```/g, '').trim();
            return JSON.parse(cleaned);
        } catch (e) {
            console.error('Failed to parse json story:', text);
            return {
                title: 'روایتی از تاریکی',
                genre: genre || 'ماورایی',
                synopsis: text.slice(0, 100) + '...',
                content: text
            };
        }
    },

    async generate12GrimFortunes(customPrompt = null) {
        const key = await this.resolveApiKey();
        if (!key) throw new Error('کلید Gemini API در تب «تنظیم AI» یا دیتابیس یافت نشد.');
        const model = this.getModel();

        const promptText = customPrompt || localStorage.getItem('HORROR_GRIM_PROMPT') || DEFAULT_GRIM_FORTUNE_PROMPT;

        const url = `https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent?key=${key}`;
        const response = await fetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                contents: [{ parts: [{ text: promptText }] }],
                generationConfig: {
                    temperature: 0.9,
                    maxOutputTokens: 8192
                }
            })
        });

        if (!response.ok) {
            const err = await response.text();
            throw new Error(`خطای تولید طالع (${response.status}): ${err}`);
        }

        const data = await response.json();
        const rawText = data.candidates?.[0]?.content?.parts?.[0]?.text;
        if (!rawText) throw new Error('پاسخی از هوش مصنوعی دریافت نشد.');

        return this.parseBatchFortunes(rawText);
    },

    parseBatchFortunes(rawText) {
        const months = [
            "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
            "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
        ];
        const results = [];

        for (let i = 1; i <= 12; i++) {
            const marker = `===${i}===`;
            const nextMarker = `===${i + 1}===`;
            
            let block = "";
            if (rawText.includes(marker)) {
                const start = rawText.indexOf(marker) + marker.length;
                const end = (i < 12 && rawText.includes(nextMarker)) ? rawText.indexOf(nextMarker) : rawText.length;
                block = rawText.substring(start, end).trim();
            }

            let title = `طالع شوم ماه ${months[i - 1]}`;
            let poem = "شنیده‌ام که در این دخمه چون قدم بنهی / ز بوی مرگ و تباهی دمی نیاسایی";
            let fortune = "سایه‌های شوم در این ماه بر سرنوشت شما سایه افکنده‌اند. از تصمیمات ناگهانی و سفر در تاریکی بپرهیزید.";
            let doomLevel = "بسیار شوم";

            if (block) {
                const lines = block.split('\n').map(l => l.trim()).filter(Boolean);
                for (const line of lines) {
                    if (line.startsWith("عنوان:")) title = line.replace("عنوان:", "").trim();
                    else if (line.startsWith("شعر:")) poem = line.replace("شعر:", "").trim();
                    else if (line.startsWith("طالع:")) fortune = line.replace("طالع:", "").trim();
                    else if (line.startsWith("درجه:")) doomLevel = line.replace("درجه:", "").trim();
                    else if (fortune.length < 50) fortune += " " + line;
                }
            }

            results.push({
                month_index: i,
                month_name: months[i - 1],
                title,
                omen_poem: poem,
                fortune_text: fortune,
                doom_level: doomLevel
            });
        }

        return results;
    }
};

window.GeminiService = GeminiService;
window.SUPPORTED_GEMINI_MODELS = SUPPORTED_GEMINI_MODELS;
window.DEFAULT_AI_STORY_PROMPT = DEFAULT_AI_STORY_PROMPT;
window.DEFAULT_GRIM_FORTUNE_PROMPT = DEFAULT_GRIM_FORTUNE_PROMPT;
