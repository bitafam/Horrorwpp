/**
 * Supabase Service Client for HorrorHouse Web Admin
 * Full REST API integration mirroring Android Retrofit SupabaseApi
 */

const SupabaseService = {
    getUrl() {
        const stored = localStorage.getItem('HORROR_SUPABASE_URL');
        const configVal = window.APP_CONFIG && window.APP_CONFIG.SUPABASE_URL;
        return (stored || configVal || '').trim().replace(/\/+$/, '');
    },
    getAnonKey() {
        const stored = localStorage.getItem('HORROR_SUPABASE_KEY');
        const configVal = window.APP_CONFIG && window.APP_CONFIG.SUPABASE_ANON_KEY;
        return (stored || configVal || '').trim();
    },
    getToken() {
        return localStorage.getItem('HORROR_ADMIN_TOKEN') || this.getAnonKey();
    },
    getUserEmail() {
        return localStorage.getItem('HORROR_ADMIN_EMAIL') || 'admin@horrorhouse.com';
    },

    saveConfig(url, anonKey) {
        const cleanUrl = (url || '').trim().replace(/\/+$/, '');
        const cleanKey = (anonKey || '').trim();
        localStorage.setItem('HORROR_SUPABASE_URL', cleanUrl);
        localStorage.setItem('HORROR_SUPABASE_KEY', cleanKey);
        if (window.APP_CONFIG) {
            window.APP_CONFIG.SUPABASE_URL = cleanUrl;
            window.APP_CONFIG.SUPABASE_ANON_KEY = cleanKey;
        }
    },

    isConfigured() {
        const url = this.getUrl();
        const key = this.getAnonKey();
        return Boolean(url && key && !url.includes('your-project') && !key.includes('your-supabase') && url.startsWith('http'));
    },

    getHeaders() {
        const key = this.getAnonKey();
        const token = this.getToken();
        return {
            'Content-Type': 'application/json',
            'apikey': key,
            'Authorization': `Bearer ${token || key}`,
            'Prefer': 'return=representation'
        };
    },

    async request(endpoint, options = {}) {
        if (!this.isConfigured()) {
            throw new Error('پایگاه داده Supabase متصل نیست. لطفاً آدرس و کلید را تنظیم کنید.');
        }

        const url = `${this.getUrl()}/${endpoint}`;
        const response = await fetch(url, {
            ...options,
            headers: {
                ...this.getHeaders(),
                ...(options.headers || {})
            }
        });

        if (!response.ok) {
            const err = await response.text();
            throw new Error(`خطای Supabase (${response.status}): ${err}`);
        }

        if (response.status === 204) return null;
        return await response.json();
    },

    // ----------------------------------------------------
    // AUTH & CONNECTION
    // ----------------------------------------------------
    async directConnect(url, key) {
        this.saveConfig(url, key);
        if (!this.isConfigured()) {
            throw new Error('آدرس یا کلید نامعتبر است.');
        }
        await this.testConnection();
        localStorage.setItem('HORROR_IS_LOGGED_IN', 'true');
        localStorage.setItem('HORROR_ADMIN_EMAIL', 'admin@supabase');
        return { success: true };
    },

    async login(email, password) {
        if (!this.isConfigured()) {
            throw new Error('ابتدا آدرس و کلید Supabase را وارد کنید.');
        }

        // Try Supabase Auth first
        try {
            const url = `${this.getUrl()}/auth/v1/token?grant_type=password`;
            const response = await fetch(url, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'apikey': this.getAnonKey()
                },
                body: JSON.stringify({ email, password })
            });

            if (response.ok) {
                const data = await response.json();
                localStorage.setItem('HORROR_ADMIN_TOKEN', data.access_token);
                localStorage.setItem('HORROR_ADMIN_EMAIL', data.user?.email || email);
                localStorage.setItem('HORROR_IS_LOGGED_IN', 'true');
                return data;
            }
        } catch (e) {
            console.warn('Auth attempt error, checking direct DB connection:', e);
        }

        // If auth failed, verify if database table access works directly via API key
        await this.testConnection();
        localStorage.setItem('HORROR_ADMIN_EMAIL', email || 'admin@horrorhouse.com');
        localStorage.setItem('HORROR_IS_LOGGED_IN', 'true');
        return { email, direct: true };
    },

    logout() {
        localStorage.removeItem('HORROR_ADMIN_TOKEN');
        localStorage.removeItem('HORROR_IS_LOGGED_IN');
    },

    isLoggedIn() {
        return localStorage.getItem('HORROR_IS_LOGGED_IN') === 'true';
    },

    async testConnection() {
        if (!this.isConfigured()) throw new Error('آدرس و کلید معتبری برای Supabase ثبت نشده است.');
        // First try app_settings, fallback to real_stories
        try {
            const res = await this.request('rest/v1/app_settings?select=key&limit=1');
            return 'اتصال با پایگاه داده برقرار است و تنظیمات با موفقیت خوانده شد.';
        } catch (e) {
            const res2 = await this.request('rest/v1/real_stories?select=id&limit=1');
            return 'اتصال با پایگاه داده با موفقیت برقرار شد.';
        }
    },

    // ----------------------------------------------------
    // REAL STORIES
    // ----------------------------------------------------
    async getRealStories() {
        return await this.request('rest/v1/real_stories?select=*&order=created_at.desc');
    },

    async insertRealStory(story) {
        return await this.request('rest/v1/real_stories', {
            method: 'POST',
            body: JSON.stringify(story)
        });
    },

    async updateRealStory(id, fields) {
        return await this.request(`rest/v1/real_stories?id=eq.${id}`, {
            method: 'PATCH',
            body: JSON.stringify(fields)
        });
    },

    async deleteRealStory(id) {
        return await this.request(`rest/v1/real_stories?id=eq.${id}`, {
            method: 'DELETE'
        });
    },

    async bulkDeleteRealStories(ids) {
        return await this.request(`rest/v1/real_stories?id=in.(${ids.join(',')})`, {
            method: 'DELETE'
        });
    },

    async bulkUpdateRealStoriesStatus(ids, status) {
        return await this.request(`rest/v1/real_stories?id=in.(${ids.join(',')})`, {
            method: 'PATCH',
            body: JSON.stringify({ status })
        });
    },

    // ----------------------------------------------------
    // USER SUBMISSIONS
    // ----------------------------------------------------
    async getSubmissions() {
        return await this.request('rest/v1/user_story_submissions?select=*&order=created_at.desc');
    },

    async updateSubmission(id, fields) {
        return await this.request(`rest/v1/user_story_submissions?id=eq.${id}`, {
            method: 'PATCH',
            body: JSON.stringify(fields)
        });
    },

    async deleteSubmission(id) {
        return await this.request(`rest/v1/user_story_submissions?id=eq.${id}`, {
            method: 'DELETE'
        });
    },

    // ----------------------------------------------------
    // GRIM FORTUNES (12 Months)
    // ----------------------------------------------------
    async getGrimFortunes() {
        return await this.request('rest/v1/grim_fortunes?select=*&order=month_index.asc');
    },

    async upsertGrimFortune(item) {
        return await this.request('rest/v1/grim_fortunes?on_conflict=month_index', {
            method: 'POST',
            body: JSON.stringify(item)
        });
    },

    async upsertAllGrimFortunes(items) {
        return await this.request('rest/v1/grim_fortunes?on_conflict=month_index', {
            method: 'POST',
            body: JSON.stringify(items)
        });
    },

    // ----------------------------------------------------
    // AI STORIES
    // ----------------------------------------------------
    async getAiStories() {
        return await this.request('rest/v1/ai_stories?select=*&order=created_at.desc');
    },

    async insertAiStory(story) {
        return await this.request('rest/v1/ai_stories', {
            method: 'POST',
            body: JSON.stringify(story)
        });
    },

    async updateAiStory(id, fields) {
        return await this.request(`rest/v1/ai_stories?id=eq.${id}`, {
            method: 'PATCH',
            body: JSON.stringify(fields)
        });
    },

    async deleteAiStory(id) {
        return await this.request(`rest/v1/ai_stories?id=eq.${id}`, {
            method: 'DELETE'
        });
    },

    async bulkDeleteAiStories(ids) {
        return await this.request(`rest/v1/ai_stories?id=in.(${ids.join(',')})`, {
            method: 'DELETE'
        });
    },

    async bulkUpdateAiStoriesStatus(ids, status) {
        return await this.request(`rest/v1/ai_stories?id=in.(${ids.join(',')})`, {
            method: 'PATCH',
            body: JSON.stringify({ status })
        });
    },

    // ----------------------------------------------------
    // PROMPTS & SETTINGS (App Settings & Gemini Key)
    // ----------------------------------------------------
    async getAppSettings() {
        return await this.request('rest/v1/app_settings?select=*');
    },

    async upsertAppSetting(key, value, description = '') {
        return await this.request('rest/v1/app_settings?on_conflict=key', {
            method: 'POST',
            body: JSON.stringify({
                key,
                value: String(value ?? ''),
                description,
                updated_at: new Date().toISOString()
            })
        });
    },

    async getAppSetting(key, defaultValue = '') {
        try {
            const rows = await this.request(`rest/v1/app_settings?key=eq.${encodeURIComponent(key)}`);
            if (rows && rows.length > 0) return rows[0].value ?? defaultValue;
        } catch (e) {
            console.warn(`Error getting app setting ${key}:`, e);
        }
        return defaultValue;
    },

    async getAiPrompts() {
        return await this.request('rest/v1/ai_prompts?select=*');
    },

    async upsertAiPrompt(promptKey, text) {
        return await this.request('rest/v1/ai_prompts?on_conflict=prompt_key', {
            method: 'POST',
            body: JSON.stringify({
                prompt_key: promptKey,
                prompt_text: text,
                updated_at: new Date().toISOString()
            })
        });
    },

    async getSubscriptionPrice() {
        const rows = await this.request('rest/v1/app_settings?key=eq.subscription_price_toman');
        if (rows && rows.length > 0) {
            return parseInt(rows[0].value, 10) || 49000;
        }
        return 49000;
    },

    async updateSubscriptionPrice(priceToman) {
        return await this.request('rest/v1/app_settings?on_conflict=key', {
            method: 'POST',
            body: JSON.stringify({
                key: 'subscription_price_toman',
                value: priceToman.toString(),
                description: 'قیمت خرید اشتراک دائمی به تومان',
                updated_at: new Date().toISOString()
            })
        });
    },

    // ----------------------------------------------------
    // USER REPORTS
    // ----------------------------------------------------
    async getReports() {
        return await this.request('rest/v1/story_reports?select=*&order=created_at.desc');
    },

    async deleteReport(id) {
        return await this.request(`rest/v1/story_reports?id=eq.${id}`, {
            method: 'DELETE'
        });
    },

    async bulkDeleteReports(ids) {
        return await this.request(`rest/v1/story_reports?id=in.(${ids.join(',')})`, {
            method: 'DELETE'
        });
    },

    // ----------------------------------------------------
    // AUTOMATION
    // ----------------------------------------------------
    async getAutomationConfigs() {
        return await this.request('rest/v1/automation_configs?select=*');
    },

    async upsertAutomationConfig(config) {
        return await this.request('rest/v1/automation_configs?on_conflict=task_name', {
            method: 'POST',
            body: JSON.stringify(config)
        });
    },

    async getAutomationLogs() {
        return await this.request('rest/v1/automation_logs?select=*&order=created_at.desc&limit=30');
    },

    async triggerEdgeFunction(functionName, payload = { manual: true }) {
        const url = `${this.getUrl()}/functions/v1/${functionName}`;
        const res = await fetch(url, {
            method: 'POST',
            headers: this.getHeaders(),
            body: JSON.stringify(payload)
        });
        if (!res.ok) {
            const err = await res.text();
            throw new Error(`Edge Function error (${res.status}): ${err}`);
        }
        return await res.json().catch(() => ({ success: true }));
    }
};

window.SupabaseService = SupabaseService;
