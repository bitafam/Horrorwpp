/**
 * Supabase Service Client for HorrorHouse Web Admin
 * Full REST API integration mirroring Android Retrofit SupabaseApi
 */

const SupabaseService = {
    getUrl() {
        return (localStorage.getItem('HORROR_SUPABASE_URL') || '').trim().replace(/\/+$/, '');
    },
    getAnonKey() {
        return (localStorage.getItem('HORROR_SUPABASE_KEY') || '').trim();
    },
    getToken() {
        return localStorage.getItem('HORROR_ADMIN_TOKEN') || this.getAnonKey();
    },
    getUserEmail() {
        return localStorage.getItem('HORROR_ADMIN_EMAIL') || '';
    },

    saveConfig(url, anonKey) {
        localStorage.setItem('HORROR_SUPABASE_URL', (url || '').trim().replace(/\/+$/, ''));
        localStorage.setItem('HORROR_SUPABASE_KEY', (anonKey || '').trim());
    },

    isConfigured() {
        const url = this.getUrl();
        const key = this.getAnonKey();
        return Boolean(url && key && !url.includes('your-project') && !key.includes('your-supabase'));
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
            throw new Error('Supabase پیکربندی نشده است. لطفاً آدرس و کلید را در تب «تنظیم AI» وارد کنید.');
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
    // AUTH
    // ----------------------------------------------------
    async login(email, password) {
        if (!this.isConfigured()) {
            // Demo/Offline bypass if not configured
            localStorage.setItem('HORROR_ADMIN_EMAIL', email);
            localStorage.setItem('HORROR_IS_LOGGED_IN', 'true');
            return { email, offline: true };
        }

        const url = `${this.getUrl()}/auth/v1/token?grant_type=password`;
        const response = await fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'apikey': this.getAnonKey()
            },
            body: JSON.stringify({ email, password })
        });

        if (!response.ok) {
            const err = await response.json().catch(() => ({}));
            throw new Error(err.error_description || err.msg || 'ایمیل یا رمز عبور نامعتبر است.');
        }

        const data = await response.json();
        localStorage.setItem('HORROR_ADMIN_TOKEN', data.access_token);
        localStorage.setItem('HORROR_ADMIN_EMAIL', data.user?.email || email);
        localStorage.setItem('HORROR_IS_LOGGED_IN', 'true');
        return data;
    },

    logout() {
        localStorage.removeItem('HORROR_ADMIN_TOKEN');
        localStorage.removeItem('HORROR_IS_LOGGED_IN');
    },

    isLoggedIn() {
        return localStorage.getItem('HORROR_IS_LOGGED_IN') === 'true';
    },

    async testConnection() {
        if (!this.isConfigured()) throw new Error('آدرس یا کلید نامعتبر است.');
        // Try fetching 1 row from real_stories or ai_stories
        const res = await this.request('rest/v1/real_stories?select=id&limit=1');
        return `اتصال پایگاه داده برقرار است (${res.length} نمونه یافت شد).`;
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
    // PROMPTS & SETTINGS
    // ----------------------------------------------------
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
