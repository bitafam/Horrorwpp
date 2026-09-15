/**
 * Horror House Web Admin Panel - Main Controller
 * 100% Faithful Replica of Jetpack Compose AdminScreens.kt & HorrorViewModel
 */

// ====================================================
// STATE & CACHE
// ====================================================
let realStoriesCache = [];
let submissionsCache = [];
let aiStoriesCache = [];
let fortunesCache = [];
let reportsCache = [];
let automationLogsCache = [];

let selectedRealStoryIds = new Set();
let selectedAiStoryIds = new Set();
let selectedReportIds = new Set();

let activeRealStatusFilter = 'ALL';
let activeSubStatusFilter = 'ALL';
let activeAiStatusFilter = 'ALL';
let activeAiSortFilter = 'NEWEST';

let selectedMonthIndex = 1; // 1 to 12
let selectedAiGenCount = 1;
let selectedAiGenGenre = 'ماورایی';

const MONTH_NAMES = [
    "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
    "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
];

// Fallback seed fortunes for 12 months if none in database yet
const DEFAULT_FORTUNES = MONTH_NAMES.map((name, idx) => ({
    month_index: idx + 1,
    month_name: name,
    title: `طالع شوم ماه ${name}`,
    omen_poem: "در این شب سیاهم گم گشت راه مقصود / از گوشه‌ای برون آی ای کوکب هدایت",
    fortune_text: `متولدین ماه ${name} در ماه‌های پیش‌رو با سایه‌هایی از گذشته روبرو خواهند شد. هرگز در نیمه‌شب تنها قدم در خانه‌های ناشناخته نگذارید.`,
    doom_level: idx % 3 === 0 ? "نفرین ابدی" : (idx % 2 === 0 ? "بسیار شوم" : "شوم"),
    status: "PUBLISHED"
}));

// ====================================================
// INITIALIZATION
// ====================================================
document.addEventListener('DOMContentLoaded', () => {
    initAuth();
    initNavigation();
    initModals();
    initDashboard();
    initStoriesTab();
    initGrimFortuneTab();
    initAiStoriesTab();
    initAiSettingsTab();
    initAutomationTab();
    initReportsTab();

    if (SupabaseService.isLoggedIn()) {
        showAdminPanel();
        loadAllData();
    } else {
        showLoginScreen();
    }
});

// Toast notification helper
function showToast(message, type = 'success') {
    const container = document.getElementById('toastContainer');
    const toast = document.createElement('div');
    toast.className = `toast-msg ${type}`;
    toast.innerHTML = `<span>${type === 'success' ? '✅' : '⚠️'}</span> <span>${message}</span>`;
    container.appendChild(toast);
    setTimeout(() => toast.remove(), 4000);
}

// Modal open/close helpers
function openModal(modalId) {
    document.getElementById(modalId)?.classList.add('active');
}
function closeModal(modalId) {
    document.getElementById(modalId)?.classList.remove('active');
}

function initModals() {
    document.querySelectorAll('[data-close]').forEach(btn => {
        btn.addEventListener('click', () => {
            const modalId = btn.getAttribute('data-close');
            closeModal(modalId);
        });
    });
    document.querySelectorAll('.modal-backdrop').forEach(overlay => {
        overlay.addEventListener('click', (e) => {
            if (e.target === overlay) {
                overlay.classList.remove('active');
            }
        });
    });
}

// ====================================================
// 1. AUTH & SCREEN SWITCHING
// ====================================================
function initAuth() {
    const btnToggle = document.getElementById('btnToggleLoginPass');
    const passInput = document.getElementById('loginPassword');
    btnToggle?.addEventListener('click', () => {
        const isPass = passInput.type === 'password';
        passInput.type = isPass ? 'text' : 'password';
        btnToggle.textContent = isPass ? '🔒' : '👁️';
    });

    document.getElementById('btnLoginSubmit')?.addEventListener('click', async () => {
        const email = document.getElementById('loginEmail').value.trim();
        const pass = document.getElementById('loginPassword').value.trim();
        const feedback = document.getElementById('loginFeedback');
        const btn = document.getElementById('btnLoginSubmit');

        if (!email || !pass) {
            feedback.className = 'login-feedback error';
            feedback.textContent = 'لطفاً ایمیل و رمز عبور را وارد نمایید.';
            return;
        }

        btn.disabled = true;
        btn.innerHTML = '<span>در حال بررسی اعتبار...</span>';
        feedback.className = 'login-feedback';
        feedback.style.display = 'none';

        try {
            await SupabaseService.login(email, pass);
            feedback.className = 'login-feedback success';
            feedback.textContent = 'ورود با موفقیت انجام شد.';
            setTimeout(() => {
                showAdminPanel();
                loadAllData();
            }, 500);
        } catch (err) {
            feedback.className = 'login-feedback error';
            feedback.textContent = err.message || 'خطا در ورود به سیستم.';
        } finally {
            btn.disabled = false;
            btn.innerHTML = '<span>ورود به پنل مدیریت</span>';
        }
    });

    document.getElementById('btnTopLogout')?.addEventListener('click', () => {
        if (confirm('آیا از خروج از پنل مدیریت اطمینان دارید؟')) {
            SupabaseService.logout();
            showLoginScreen();
            showToast('با موفقیت از پنل مدیریت خارج شدید.');
        }
    });

    document.getElementById('btnTopRefresh')?.addEventListener('click', () => {
        loadAllData(true);
    });
}

function showLoginScreen() {
    document.getElementById('loginScreenContainer').style.display = 'flex';
    document.getElementById('adminPanelContainer').style.display = 'none';
}

function showAdminPanel() {
    document.getElementById('loginScreenContainer').style.display = 'none';
    document.getElementById('adminPanelContainer').style.display = 'flex';
    updateTopBarActiveModel();
}

function updateTopBarActiveModel() {
    const el = document.getElementById('topBarActiveModel');
    if (el) el.textContent = GeminiService.getModel();
}

// ====================================================
// 2. NAVIGATION (7 Jetpack Compose Tabs)
// ====================================================
function initNavigation() {
    const navButtons = document.querySelectorAll('.compose-nav-bar .nav-tab-button');
    navButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            navButtons.forEach(b => b.classList.remove('active'));
            btn.classList.add('active');

            const tabId = btn.getAttribute('data-tab');
            document.querySelectorAll('.tab-pane').forEach(p => p.classList.remove('active'));
            document.getElementById(tabId)?.classList.add('active');
        });
    });
}

// ====================================================
// 3. LOAD ALL DATA
// ====================================================
async function loadAllData(showToastMsg = false) {
    if (showToastMsg) showToast('در حال همگام‌سازی اطلاعات با سرور...');

    // 1. Diagnostics
    checkDiagnostics();

    // 2. Real Stories
    try {
        const stories = await SupabaseService.getRealStories();
        if (stories && stories.length >= 0) realStoriesCache = stories;
    } catch (e) {
        console.warn('Using local real stories fallback:', e);
        if (realStoriesCache.length === 0) {
            realStoriesCache = [
                {
                    id: "sample-1",
                    title: "خانه قدیمی کوچه ارامنه",
                    content: "در زمستان سال ۱۳۶۸ در کوچه‌ای بن‌بست در محله قدیمی ارامنه تبریز، صدایی شبیه کوبیده شدن شیء فلزی بر کف حیاط شنیده می‌شد...",
                    author: "فرهاد ناظمی",
                    source: "روایات محلی آذربایجان",
                    cover_image_url: "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=500&auto=format&fit=crop&q=80",
                    tags: "تبریز, ارامنه, خانه متروکه",
                    status: "PUBLISHED",
                    views_count: 1420
                },
                {
                    id: "sample-2",
                    title: "سایه پشت پرده طبقه چهارم",
                    content: "ساختمان نوساز بود و هنوز بیشتر واحدها خالی بودند. شب‌ها که به خانه برمی‌گشتم، از پنجره طبقه چهارم نوری آبی‌رنگ سوسو می‌زد...",
                    author: "کاتب عمارت",
                    source: "روایات شهری",
                    cover_image_url: "https://images.unsplash.com/photo-1509248961158-e54f6934749c?w=500&auto=format&fit=crop&q=80",
                    tags: "آپارتمان, سایه, تهران",
                    status: "PUBLISHED",
                    views_count: 890
                }
            ];
        }
    }

    // 3. Submissions
    try {
        const subs = await SupabaseService.getSubmissions();
        if (subs) submissionsCache = subs;
    } catch (e) {
        console.warn('Submissions fallback:', e);
    }

    // 4. Fortunes
    try {
        const forts = await SupabaseService.getGrimFortunes();
        if (forts && forts.length > 0) {
            fortunesCache = forts;
        } else {
            fortunesCache = DEFAULT_FORTUNES;
        }
    } catch (e) {
        fortunesCache = DEFAULT_FORTUNES;
    }

    // 5. AI Stories
    try {
        const ai = await SupabaseService.getAiStories();
        if (ai) aiStoriesCache = ai;
    } catch (e) {
        console.warn('AI stories fallback:', e);
        if (aiStoriesCache.length === 0) {
            aiStoriesCache = [
                {
                    id: "ai-sample-1",
                    title: "چاه خاموش مرنجاب",
                    genre: "کویر و بیابان",
                    synopsis: "کاروانی در حاشیه کویر مرنجاب بر سر چاهی فرود می‌آیند که آب آن قرن‌هاست خشکیده، اما زمزمه‌هایی از درون آن طنین‌انداز است.",
                    content: "باد گرم کویر دانه‌های ریز شن را به صورت می‌کوبید. چاهی باستانی در میان تپه‌های ماسه‌ای قد علم کرده بود...",
                    status: "PUBLISHED",
                    rating: 4.8,
                    view_count: 512
                }
            ];
        }
    }

    // 6. Subscription Price
    try {
        const price = await SupabaseService.getSubscriptionPrice();
        document.getElementById('badgeCurrentPrice').textContent = `${price.toLocaleString('fa-IR')} تومان`;
        document.getElementById('inputPriceToman').value = price;
    } catch (e) {
        console.warn('Price fallback:', e);
    }

    // 7. Reports
    try {
        const reports = await SupabaseService.getReports();
        if (reports) reportsCache = reports;
    } catch (e) {
        console.warn('Reports fallback:', e);
    }

    // 8. Automation logs
    try {
        const logs = await SupabaseService.getAutomationLogs();
        if (logs) automationLogsCache = logs;
    } catch (e) {
        console.warn('Automation logs fallback:', e);
    }

    // Re-render UI views
    renderDashboard();
    renderRealStories();
    renderSubmissions();
    renderGrimFortunesGrid();
    renderSelectedMonthFortune();
    renderAiStories();
    renderReports();
    renderAutomationLogs();

    if (showToastMsg) showToast('تمامی اطلاعات به‌روزرسانی شدند.');
}

async function checkDiagnostics() {
    const badgeDb = document.getElementById('statusBadgeSupabase');
    const badgeAi = document.getElementById('statusBadgeGemini');

    if (SupabaseService.isConfigured()) {
        try {
            await SupabaseService.testConnection();
            badgeDb.className = 'badge published';
            badgeDb.textContent = 'پایگاه داده: متصل (Supabase)';
        } catch (e) {
            badgeDb.className = 'badge draft';
            badgeDb.textContent = 'پایگاه داده: خطا در ارتباط';
        }
    } else {
        badgeDb.className = 'badge blood';
        badgeDb.textContent = 'پایگاه داده: پیکربندی نشده (حالت محلی)';
    }

    if (GeminiService.getApiKey()) {
        badgeAi.className = 'badge published';
        badgeAi.textContent = `هوش مصنوعی: آماده (${GeminiService.getModel()})`;
    } else {
        badgeAi.className = 'badge blood';
        badgeAi.textContent = 'هوش مصنوعی: کلید وارد نشده';
    }
}

// ====================================================
// 4. TAB 0: DASHBOARD
// ====================================================
function initDashboard() {
    document.getElementById('btnQuickNewStory')?.addEventListener('click', () => {
        document.querySelector('[data-tab="tab-stories"]').click();
        document.getElementById('btnOpenNewStory').click();
    });
    document.getElementById('btnQuickBatchFortune')?.addEventListener('click', () => {
        document.querySelector('[data-tab="tab-fortune"]').click();
        document.getElementById('btnGenerateAllFortunes').click();
    });
    document.getElementById('btnQuickAiStories')?.addEventListener('click', () => {
        document.querySelector('[data-tab="tab-ai-stories"]').click();
    });
}

function renderDashboard() {
    document.getElementById('statRealCount').textContent = realStoriesCache.length.toLocaleString('fa-IR');
    document.getElementById('statSubmissionsCount').textContent = submissionsCache.length.toLocaleString('fa-IR');
    document.getElementById('statFortunesCount').textContent = fortunesCache.length.toLocaleString('fa-IR');
    document.getElementById('statAiCount').textContent = aiStoriesCache.length.toLocaleString('fa-IR');

    const pendingSubmissions = submissionsCache.filter(s => s.status === 'PENDING').length;
    const badgeSub = document.getElementById('badgeSubmissionsCount');
    if (pendingSubmissions > 0) {
        badgeSub.style.display = 'inline-block';
        badgeSub.textContent = pendingSubmissions;
    } else {
        badgeSub.style.display = 'none';
    }
}

// ====================================================
// 5. TAB 1: STORIES MANAGER (AdminStoriesManagerTab)
// ====================================================
function initStoriesTab() {
    // SubTabs
    const btnReal = document.getElementById('btnSubTabReal');
    const btnSubs = document.getElementById('btnSubTabSubmissions');
    btnReal.addEventListener('click', () => {
        btnReal.classList.add('active');
        btnSubs.classList.remove('active');
        document.getElementById('realStoriesSubView').style.display = 'block';
        document.getElementById('submissionsSubView').style.display = 'none';
    });
    btnSubs.addEventListener('click', () => {
        btnSubs.classList.add('active');
        btnReal.classList.remove('active');
        document.getElementById('realStoriesSubView').style.display = 'none';
        document.getElementById('submissionsSubView').style.display = 'block';
    });

    // Real Stories Status Filters
    document.querySelectorAll('[data-filter-status]').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('[data-filter-status]').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            activeRealStatusFilter = btn.getAttribute('data-filter-status');
            renderRealStories();
        });
    });

    // Real Search
    document.getElementById('searchRealStories')?.addEventListener('input', () => {
        renderRealStories();
    });

    // Single Add/Edit Modal
    document.getElementById('btnOpenNewStory')?.addEventListener('click', () => {
        document.getElementById('formStoryId').value = '';
        document.getElementById('modalStoryTitle').textContent = 'ثبت داستان واقعی جدید با پوستر';
        document.getElementById('formStoryTitle').value = '';
        document.getElementById('formStoryAuthor').value = 'کاتب عمارت';
        document.getElementById('formStorySource').value = 'روایات واقعی';
        document.getElementById('formStoryCover').value = '';
        document.getElementById('formStoryTags').value = 'وحشت, واقعی';
        document.getElementById('formStoryContent').value = '';
        document.getElementById('formStoryStatus').value = 'PUBLISHED';
        openModal('modalStoryForm');
    });

    document.getElementById('btnSaveStoryForm')?.addEventListener('click', async () => {
        const id = document.getElementById('formStoryId').value.trim() || `real-${Date.now()}`;
        const title = document.getElementById('formStoryTitle').value.trim();
        const author = document.getElementById('formStoryAuthor').value.trim() || 'کاتب عمارت';
        const source = document.getElementById('formStorySource').value.trim() || 'روایات واقعی';
        const cover = document.getElementById('formStoryCover').value.trim() || 'https://images.unsplash.com/photo-1509248961158-e54f6934749c?w=500&auto=format&fit=crop&q=80';
        const tags = document.getElementById('formStoryTags').value.trim() || 'وحشت, واقعی';
        const content = document.getElementById('formStoryContent').value.trim();
        const status = document.getElementById('formStoryStatus').value;

        if (!title || !content) {
            showToast('لطفاً عنوان و متن داستان را تکمیل کنید.', 'error');
            return;
        }

        const storyObj = {
            id,
            title,
            author,
            source,
            cover_image_url: cover,
            tags,
            content,
            status,
            updated_at: new Date().toISOString()
        };

        try {
            if (SupabaseService.isConfigured()) {
                await SupabaseService.insertRealStory(storyObj);
            }
            // Update local cache
            const existingIdx = realStoriesCache.findIndex(s => s.id === id);
            if (existingIdx >= 0) {
                realStoriesCache[existingIdx] = { ...realStoriesCache[existingIdx], ...storyObj };
            } else {
                realStoriesCache.unshift(storyObj);
            }
            closeModal('modalStoryForm');
            renderRealStories();
            renderDashboard();
            showToast('داستان واقعی با موفقیت ذخیره شد.');
        } catch (e) {
            showToast(`خطا در ذخیره داستان: ${e.message}`, 'error');
        }
    });

    // Bulk Add Modal (parseBulkStories)
    document.getElementById('btnOpenBulkAddStory')?.addEventListener('click', () => {
        openModal('modalBulkStories');
    });

    document.getElementById('btnExecuteBulkAdd')?.addEventListener('click', async () => {
        const rawText = document.getElementById('txtBulkStoriesInput').value.trim();
        if (!rawText) {
            showToast('لطفاً متن داستان‌ها را وارد کنید.', 'error');
            return;
        }

        const parsedList = parseBulkStories(rawText);
        if (parsedList.length === 0) {
            showToast('هیچ داستانی شناسایی نشد. لطفاً ساختار را بررسی کنید.', 'error');
            return;
        }

        try {
            if (SupabaseService.isConfigured()) {
                for (const item of parsedList) {
                    await SupabaseService.insertRealStory(item);
                }
            }
            realStoriesCache = [...parsedList, ...realStoriesCache];
            closeModal('modalBulkStories');
            document.getElementById('txtBulkStoriesInput').value = '';
            renderRealStories();
            renderDashboard();
            showToast(`${parsedList.length} داستان گروهی با موفقیت اضافه شد.`);
        } catch (e) {
            showToast(`خطا در افزودن گروهی: ${e.message}`, 'error');
        }
    });

    // Bulk Actions Selection Bar
    document.getElementById('checkSelectAllReal')?.addEventListener('change', (e) => {
        const checked = e.target.checked;
        selectedRealStoryIds.clear();
        if (checked) {
            realStoriesCache.forEach(s => selectedRealStoryIds.add(s.id));
        }
        updateRealBulkBar();
        renderRealStories();
    });

    document.getElementById('btnBulkPublishReal')?.addEventListener('click', async () => {
        const ids = Array.from(selectedRealStoryIds);
        if (ids.length === 0) return;
        try {
            if (SupabaseService.isConfigured()) {
                await SupabaseService.bulkUpdateRealStoriesStatus(ids, 'PUBLISHED');
            }
            realStoriesCache.forEach(s => {
                if (ids.includes(s.id)) s.status = 'PUBLISHED';
            });
            selectedRealStoryIds.clear();
            updateRealBulkBar();
            renderRealStories();
            showToast(`${ids.length} داستان منتشر شد.`);
        } catch (e) {
            showToast(`خطا: ${e.message}`, 'error');
        }
    });

    document.getElementById('btnBulkDraftReal')?.addEventListener('click', async () => {
        const ids = Array.from(selectedRealStoryIds);
        if (ids.length === 0) return;
        try {
            if (SupabaseService.isConfigured()) {
                await SupabaseService.bulkUpdateRealStoriesStatus(ids, 'DRAFT');
            }
            realStoriesCache.forEach(s => {
                if (ids.includes(s.id)) s.status = 'DRAFT';
            });
            selectedRealStoryIds.clear();
            updateRealBulkBar();
            renderRealStories();
            showToast(`${ids.length} داستان به پیش‌نویس تبدیل شد.`);
        } catch (e) {
            showToast(`خطا: ${e.message}`, 'error');
        }
    });

    document.getElementById('btnBulkDeleteReal')?.addEventListener('click', async () => {
        const ids = Array.from(selectedRealStoryIds);
        if (ids.length === 0) return;
        if (!confirm(`آیا از حذف گروهی ${ids.length} داستان اطمینان دارید؟`)) return;

        try {
            if (SupabaseService.isConfigured()) {
                await SupabaseService.bulkDeleteRealStories(ids);
            }
            realStoriesCache = realStoriesCache.filter(s => !ids.includes(s.id));
            selectedRealStoryIds.clear();
            updateRealBulkBar();
            renderRealStories();
            renderDashboard();
            showToast(`${ids.length} داستان حذف شد.`);
        } catch (e) {
            showToast(`خطا در حذف: ${e.message}`, 'error');
        }
    });

    // Submissions Status Filters
    document.querySelectorAll('[data-sub-status]').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('[data-sub-status]').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            activeSubStatusFilter = btn.getAttribute('data-sub-status');
            renderSubmissions();
        });
    });

    document.getElementById('searchSubmissions')?.addEventListener('input', () => {
        renderSubmissions();
    });
}

function updateRealBulkBar() {
    const bar = document.getElementById('realBulkBar');
    const lbl = document.getElementById('selectedRealCount');
    const chk = document.getElementById('checkSelectAllReal');

    if (selectedRealStoryIds.size > 0) {
        bar.style.display = 'flex';
        lbl.textContent = selectedRealStoryIds.size;
        chk.checked = selectedRealStoryIds.size === realStoriesCache.length;
    } else {
        bar.style.display = 'none';
        chk.checked = false;
    }
}

function parseBulkStories(inputText) {
    const rawBlocks = inputText.split("---");
    const list = [];
    for (const block of rawBlocks) {
        const trimmed = block.trim();
        if (!trimmed) continue;

        let title = "";
        let content = "";
        let poster = null;
        let author = "کاتب عمارت";
        let source = "روایات واقعی";

        const lines = trimmed.split("\n").map(l => l.trim()).filter(Boolean);
        if (lines.length === 0) continue;

        const hasKeys = lines.some(l => l.startsWith("عنوان:") || l.startsWith("متن:"));
        if (hasKeys) {
            for (const line of lines) {
                if (line.startsWith("عنوان:")) title = line.replace("عنوان:", "").trim();
                else if (line.startsWith("متن:")) content = line.replace("متن:", "").trim();
                else if (line.startsWith("پوستر:")) poster = line.replace("پوستر:", "").trim();
                else if (line.startsWith("نویسنده:")) author = line.replace("نویسنده:", "").trim();
                else if (line.startsWith("منبع:")) source = line.replace("منبع:", "").trim();
                else if (content) content += "\n" + line;
            }
        } else {
            title = lines[0];
            content = lines.slice(1).join("\n");
            if (!content) {
                content = title;
                title = title.length > 25 ? title.slice(0, 25) + "..." : title;
            }
        }

        if (title && content) {
            list.push({
                id: `real-${Date.now()}-${Math.random().toString(36).substr(2, 6)}`,
                title,
                content,
                author,
                source,
                cover_image_url: poster || "https://images.unsplash.com/photo-1509248961158-e54f6934749c?w=500&auto=format&fit=crop&q=80",
                tags: "وحشت, واقعی",
                status: "PUBLISHED",
                views_count: 0
            });
        }
    }
    return list;
}

function renderRealStories() {
    const container = document.getElementById('realStoriesListContainer');
    const query = (document.getElementById('searchRealStories')?.value || '').toLowerCase().trim();

    let filtered = realStoriesCache.filter(s => {
        const matchesStatus = activeRealStatusFilter === 'ALL' || s.status === activeRealStatusFilter;
        const matchesQuery = !query || 
            (s.title && s.title.toLowerCase().includes(query)) ||
            (s.content && s.content.toLowerCase().includes(query)) ||
            (s.tags && s.tags.toLowerCase().includes(query));
        return matchesStatus && matchesQuery;
    });

    if (filtered.length === 0) {
        container.innerHTML = `
            <div style="text-align: center; padding: 40px; color: var(--muted-ash);">
                داستانی با مشخصات جستجو شده یافت نشد.
            </div>`;
        return;
    }

    container.innerHTML = filtered.map(story => {
        const isSelected = selectedRealStoryIds.has(story.id);
        const isPublished = story.status === 'PUBLISHED';
        const coverUrl = story.cover_image_url || 'https://images.unsplash.com/photo-1509248961158-e54f6934749c?w=500&auto=format&fit=crop&q=80';

        return `
            <div class="story-card-item ${isSelected ? 'selected' : ''}" data-story-id="${story.id}">
                <div style="display: flex; align-items: center; padding-left: 4px;">
                    <input type="checkbox" class="story-checkbox" data-story-id="${story.id}" ${isSelected ? 'checked' : ''}>
                </div>
                <div class="story-poster-box">
                    <img src="${coverUrl}" class="story-poster-img" alt="${story.title}" onerror="this.src='https://images.unsplash.com/photo-1509248961158-e54f6934749c?w=500&auto=format&fit=crop&q=80'">
                </div>
                <div class="story-card-details">
                    <div class="story-card-header">
                        <div>
                            <div class="story-title-text">${story.title}</div>
                            <div class="story-meta-row" style="margin-top: 4px;">
                                <span>نویسنده: ${story.author || 'کاتب عمارت'}</span>
                                <span>منبع: ${story.source || 'روایات واقعی'}</span>
                                <span>بازدید: ${(story.views_count || 0).toLocaleString('fa-IR')}</span>
                            </div>
                        </div>
                        <span class="badge ${isPublished ? 'published' : 'draft'}">
                            ${isPublished ? 'منتشر شده' : 'پیش‌نویس'}
                        </span>
                    </div>

                    <div class="story-excerpt-text">${story.content || ''}</div>

                    <div class="story-card-actions">
                        <button class="btn btn-dark btn-sm btn-edit-story" data-story-id="${story.id}">ویرایش</button>
                        <button class="btn btn-dark btn-sm btn-toggle-publish" data-story-id="${story.id}">
                            ${isPublished ? 'تبدیل به پیش‌نویس' : 'انتشار'}
                        </button>
                        <button class="btn btn-outline-blood btn-sm btn-delete-story" data-story-id="${story.id}">حذف</button>
                    </div>
                </div>
            </div>`;
    }).join('');

    // Attach listeners
    container.querySelectorAll('.story-checkbox').forEach(chk => {
        chk.addEventListener('change', (e) => {
            const id = e.target.getAttribute('data-story-id');
            if (e.target.checked) selectedRealStoryIds.add(id);
            else selectedRealStoryIds.delete(id);
            updateRealBulkBar();
            renderRealStories();
        });
    });

    container.querySelectorAll('.btn-edit-story').forEach(btn => {
        btn.addEventListener('click', () => {
            const id = btn.getAttribute('data-story-id');
            const story = realStoriesCache.find(s => s.id === id);
            if (!story) return;

            document.getElementById('formStoryId').value = story.id;
            document.getElementById('modalStoryTitle').textContent = 'ویرایش داستان واقعی';
            document.getElementById('formStoryTitle').value = story.title || '';
            document.getElementById('formStoryAuthor').value = story.author || '';
            document.getElementById('formStorySource').value = story.source || '';
            document.getElementById('formStoryCover').value = story.cover_image_url || '';
            document.getElementById('formStoryTags').value = story.tags || '';
            document.getElementById('formStoryContent').value = story.content || '';
            document.getElementById('formStoryStatus').value = story.status || 'PUBLISHED';
            openModal('modalStoryForm');
        });
    });

    container.querySelectorAll('.btn-toggle-publish').forEach(btn => {
        btn.addEventListener('click', async () => {
            const id = btn.getAttribute('data-story-id');
            const story = realStoriesCache.find(s => s.id === id);
            if (!story) return;
            const newStatus = story.status === 'PUBLISHED' ? 'DRAFT' : 'PUBLISHED';

            try {
                if (SupabaseService.isConfigured()) {
                    await SupabaseService.updateRealStory(id, { status: newStatus });
                }
                story.status = newStatus;
                renderRealStories();
                showToast(`وضعیت به «${newStatus === 'PUBLISHED' ? 'منتشر شده' : 'پیش‌نویس'}» تغییر کرد.`);
            } catch (e) {
                showToast(`خطا: ${e.message}`, 'error');
            }
        });
    });

    container.querySelectorAll('.btn-delete-story').forEach(btn => {
        btn.addEventListener('click', async () => {
            const id = btn.getAttribute('data-story-id');
            const story = realStoriesCache.find(s => s.id === id);
            if (!story) return;
            if (!confirm(`آیا از حذف داستان «${story.title}» اطمینان دارید؟`)) return;

            try {
                if (SupabaseService.isConfigured()) {
                    await SupabaseService.deleteRealStory(id);
                }
                realStoriesCache = realStoriesCache.filter(s => s.id !== id);
                selectedRealStoryIds.delete(id);
                updateRealBulkBar();
                renderRealStories();
                renderDashboard();
                showToast('داستان با موفقیت حذف شد.');
            } catch (e) {
                showToast(`خطا در حذف: ${e.message}`, 'error');
            }
        });
    });
}

function renderSubmissions() {
    const container = document.getElementById('submissionsListContainer');
    const query = (document.getElementById('searchSubmissions')?.value || '').toLowerCase().trim();

    let filtered = submissionsCache.filter(s => {
        const matchesStatus = activeSubStatusFilter === 'ALL' || s.status === activeSubStatusFilter;
        const matchesQuery = !query || 
            (s.title && s.title.toLowerCase().includes(query)) ||
            (s.author_name && s.author_name.toLowerCase().includes(query)) ||
            (s.content && s.content.toLowerCase().includes(query));
        return matchesStatus && matchesQuery;
    });

    if (filtered.length === 0) {
        container.innerHTML = `
            <div style="text-align: center; padding: 40px; color: var(--muted-ash);">
                هیچ روایتی از طرف کاربران در این بخش وجود ندارد.
            </div>`;
        return;
    }

    container.innerHTML = filtered.map(sub => {
        const statusBadgeClass = sub.status === 'APPROVED' ? 'published' : (sub.status === 'REJECTED' ? 'blood' : 'draft');
        const statusText = sub.status === 'APPROVED' ? 'تأیید شده' : (sub.status === 'REJECTED' ? 'رد شده' : 'در انتظار بررسی');

        return `
            <div class="crypt-card" style="margin-bottom: 12px;">
                <div class="card-header-row">
                    <div>
                        <div class="story-title-text">${sub.title || 'بدون عنوان'}</div>
                        <div class="story-meta-row" style="margin-top: 4px;">
                            <span>راوی کاربر: <strong>${sub.author_name || 'کاربر ناشناس'}</strong></span>
                            <span>ایمیل/شناسه: ${sub.user_email || '—'}</span>
                            <span>تاریخ: ${sub.created_at ? new Date(sub.created_at).toLocaleDateString('fa-IR') : 'نامشخص'}</span>
                        </div>
                    </div>
                    <span class="badge ${statusBadgeClass}">${statusText}</span>
                </div>

                <div class="story-excerpt-text" style="-webkit-line-clamp: 3; margin: 8px 0;">${sub.content || ''}</div>

                <div style="display: flex; gap: 8px; flex-wrap: wrap;">
                    <button class="btn btn-blood btn-sm btn-publish-sub" data-sub-id="${sub.id}">
                        <span>✨</span> تأیید و انتشار با پوستر
                    </button>
                    <button class="btn btn-dark btn-sm btn-approve-sub" data-sub-id="${sub.id}">تأیید سریع</button>
                    <button class="btn btn-dark btn-sm btn-reject-sub" data-sub-id="${sub.id}">رد کردن</button>
                    <button class="btn btn-outline-blood btn-sm btn-delete-sub" data-sub-id="${sub.id}">حذف</button>
                </div>
            </div>`;
    }).join('');

    // Handlers
    container.querySelectorAll('.btn-publish-sub').forEach(btn => {
        btn.addEventListener('click', () => {
            const id = btn.getAttribute('data-sub-id');
            const sub = submissionsCache.find(s => s.id === id);
            if (!sub) return;

            document.getElementById('formStoryId').value = `real-${Date.now()}`;
            document.getElementById('modalStoryTitle').textContent = 'انتشار روایت کاربر در داستان‌های واقعی';
            document.getElementById('formStoryTitle').value = sub.title || '';
            document.getElementById('formStoryAuthor').value = sub.author_name || 'راوی عمارت';
            document.getElementById('formStorySource').value = 'ارسالی کاربران';
            document.getElementById('formStoryCover').value = 'https://images.unsplash.com/photo-1509248961158-e54f6934749c?w=500&auto=format&fit=crop&q=80';
            document.getElementById('formStoryTags').value = 'ارسالی, وحشت, واقعی';
            document.getElementById('formStoryContent').value = sub.content || '';
            document.getElementById('formStoryStatus').value = 'PUBLISHED';
            openModal('modalStoryForm');
        });
    });

    container.querySelectorAll('.btn-approve-sub').forEach(btn => {
        btn.addEventListener('click', async () => {
            const id = btn.getAttribute('data-sub-id');
            try {
                if (SupabaseService.isConfigured()) {
                    await SupabaseService.updateSubmission(id, { status: 'APPROVED' });
                }
                const sub = submissionsCache.find(s => s.id === id);
                if (sub) sub.status = 'APPROVED';
                renderSubmissions();
                renderDashboard();
                showToast('روایت کاربر تأیید شد.');
            } catch (e) {
                showToast(`خطا: ${e.message}`, 'error');
            }
        });
    });

    container.querySelectorAll('.btn-reject-sub').forEach(btn => {
        btn.addEventListener('click', async () => {
            const id = btn.getAttribute('data-sub-id');
            try {
                if (SupabaseService.isConfigured()) {
                    await SupabaseService.updateSubmission(id, { status: 'REJECTED' });
                }
                const sub = submissionsCache.find(s => s.id === id);
                if (sub) sub.status = 'REJECTED';
                renderSubmissions();
                renderDashboard();
                showToast('روایت کاربر رد شد.');
            } catch (e) {
                showToast(`خطا: ${e.message}`, 'error');
            }
        });
    });

    container.querySelectorAll('.btn-delete-sub').forEach(btn => {
        btn.addEventListener('click', async () => {
            const id = btn.getAttribute('data-sub-id');
            if (!confirm('آیا از حذف این ارسال اطمینان دارید؟')) return;
            try {
                if (SupabaseService.isConfigured()) {
                    await SupabaseService.deleteSubmission(id);
                }
                submissionsCache = submissionsCache.filter(s => s.id !== id);
                renderSubmissions();
                renderDashboard();
                showToast('ارسال کاربر با موفقیت حذف شد.');
            } catch (e) {
                showToast(`خطا: ${e.message}`, 'error');
            }
        });
    });
}

// ====================================================
// 6. TAB 2: GRIM FORTUNE (AdminGrimFortuneTab)
// ====================================================
function initGrimFortuneTab() {
    const btnToggle = document.getElementById('btnToggleGrimPrompt');
    const container = document.getElementById('grimPromptContainer');
    const txtPrompt = document.getElementById('txtGrimFortunePrompt');

    txtPrompt.value = localStorage.getItem('HORROR_GRIM_PROMPT') || window.DEFAULT_GRIM_FORTUNE_PROMPT;

    btnToggle.addEventListener('click', () => {
        const isHidden = container.style.display === 'none';
        container.style.display = isHidden ? 'block' : 'none';
        btnToggle.textContent = isHidden ? 'بستن پرامپت' : 'نمایش / ویرایش پرامپت';
    });

    document.getElementById('btnSaveGrimPrompt')?.addEventListener('click', async () => {
        const val = txtPrompt.value.trim();
        localStorage.setItem('HORROR_GRIM_PROMPT', val);
        try {
            if (SupabaseService.isConfigured()) {
                await SupabaseService.upsertAiPrompt('GRIM_FORTUNE_PROMPT', val);
            }
            showToast('پرامپت طالع شوم با موفقیت در دیتابیس ثبت شد.');
        } catch (e) {
            showToast(`ذخیره محلی انجام شد (${e.message})`);
        }
    });

    document.getElementById('btnResetGrimPrompt')?.addEventListener('click', () => {
        txtPrompt.value = window.DEFAULT_GRIM_FORTUNE_PROMPT;
        localStorage.setItem('HORROR_GRIM_PROMPT', window.DEFAULT_GRIM_FORTUNE_PROMPT);
        showToast('پرامپت طالع به حالت پیش‌فرض بازنشانی شد.');
    });

    // Batch 12-Month Generation
    document.getElementById('btnGenerateAllFortunes')?.addEventListener('click', async () => {
        const btn = document.getElementById('btnGenerateAllFortunes');
        const feedback = document.getElementById('fortuneGenFeedback');

        btn.disabled = true;
        btn.innerHTML = '<span>در حال احضار ۱۲ طالع شوم توسط هوش تاریکی...</span>';
        feedback.style.display = 'block';
        feedback.className = 'badge gold';
        feedback.textContent = 'ارسال پرامپت به جمینای (یک پیام واحد جهت صرفه‌جویی توکن)...';

        try {
            const promptText = txtPrompt.value.trim();
            const results = await GeminiService.generate12GrimFortunes(promptText);

            if (SupabaseService.isConfigured()) {
                await SupabaseService.upsertAllGrimFortunes(results);
            }

            fortunesCache = results;
            renderGrimFortunesGrid();
            renderSelectedMonthFortune();
            renderDashboard();

            feedback.className = 'badge published';
            feedback.textContent = 'هر ۱۲ طالع شوم با موفقیت تولید و ذخیره شدند.';
            showToast('هر ۱۲ طالع شوم سال با موفقیت تولید و در دیتابیس ثبت گردید.');
        } catch (e) {
            feedback.className = 'badge blood';
            feedback.textContent = `خطا: ${e.message}`;
            showToast(`خطا در تولید طالع: ${e.message}`, 'error');
        } finally {
            btn.disabled = false;
            btn.innerHTML = '<span>✨ تولید همزمان ۱۲ ماه با هوش تاریکی (یک پیام - صرفه‌جویی توکن)</span>';
        }
    });

    // Single Month Edit Modal
    document.getElementById('btnEditSingleMonth')?.addEventListener('click', () => {
        const item = fortunesCache.find(f => f.month_index === selectedMonthIndex);
        if (!item) return;

        document.getElementById('modalFortuneMonthName').textContent = `ویرایش طالع ماه ${MONTH_NAMES[selectedMonthIndex - 1]}`;
        document.getElementById('formFortuneMonthIndex').value = item.month_index;
        document.getElementById('formFortuneTitle').value = item.title || `طالع شوم ماه ${MONTH_NAMES[selectedMonthIndex - 1]}`;
        document.getElementById('formFortunePoem').value = item.omen_poem || '';
        document.getElementById('formFortuneDoom').value = item.doom_level || 'بسیار شوم';
        document.getElementById('formFortuneText').value = item.fortune_text || '';
        openModal('modalEditFortune');
    });

    document.getElementById('btnSaveFortuneModal')?.addEventListener('click', async () => {
        const mIdx = parseInt(document.getElementById('formFortuneMonthIndex').value, 10);
        const title = document.getElementById('formFortuneTitle').value.trim();
        const poem = document.getElementById('formFortunePoem').value.trim();
        const doom = document.getElementById('formFortuneDoom').value;
        const text = document.getElementById('formFortuneText').value.trim();

        const updated = {
            month_index: mIdx,
            month_name: MONTH_NAMES[mIdx - 1],
            title,
            omen_poem: poem,
            fortune_text: text,
            doom_level: doom
        };

        try {
            if (SupabaseService.isConfigured()) {
                await SupabaseService.upsertGrimFortune(updated);
            }
            const idx = fortunesCache.findIndex(f => f.month_index === mIdx);
            if (idx >= 0) fortunesCache[idx] = updated;
            else fortunesCache.push(updated);

            closeModal('modalEditFortune');
            renderGrimFortunesGrid();
            renderSelectedMonthFortune();
            showToast('طالع ماه با موفقیت ذخیره شد.');
        } catch (e) {
            showToast(`خطا: ${e.message}`, 'error');
        }
    });

    document.getElementById('btnRegenerateSingleMonth')?.addEventListener('click', async () => {
        const mName = MONTH_NAMES[selectedMonthIndex - 1];
        if (!confirm(`آیا می‌خواهید طالع ماه ${mName} مجدداً توسط AI تولید شود؟`)) return;

        showToast(`در حال تولید طالع ماه ${mName}...`);
        try {
            const singlePrompt = `برای ماه «${mName}» یک طالع شوم بنویس با فرمت زیر:
===1===
عنوان: [عنوان کوتاه طالع]
شعر: [بیت شعر شوم سبک حافظ]
طالع: [پیشگویی و هشدار]
درجه: [شوم / بسیار شوم / نفرین ابدی]`;

            const res = await GeminiService.generate12GrimFortunes(singlePrompt);
            if (res && res.length > 0) {
                const singleItem = {
                    ...res[0],
                    month_index: selectedMonthIndex,
                    month_name: mName
                };
                if (SupabaseService.isConfigured()) {
                    await SupabaseService.upsertGrimFortune(singleItem);
                }
                const idx = fortunesCache.findIndex(f => f.month_index === selectedMonthIndex);
                if (idx >= 0) fortunesCache[idx] = singleItem;
                else fortunesCache.push(singleItem);

                renderGrimFortunesGrid();
                renderSelectedMonthFortune();
                showToast(`طالع ماه ${mName} با موفقیت بازنویسی شد.`);
            }
        } catch (e) {
            showToast(`خطا در تولید: ${e.message}`, 'error');
        }
    });

    document.getElementById('btnToggleMonthPublish')?.addEventListener('click', () => {
        showToast('طالع‌ها به‌صورت پیش‌فرض در اپ فعال هستند.');
    });
}

function renderGrimFortunesGrid() {
    const grid = document.getElementById('monthsGrid');
    grid.innerHTML = MONTH_NAMES.map((name, idx) => {
        const monthNum = idx + 1;
        const isActive = monthNum === selectedMonthIndex;
        const fortune = fortunesCache.find(f => f.month_index === monthNum);
        const hasText = Boolean(fortune && fortune.fortune_text && fortune.fortune_text.length > 20);

        return `
            <div class="month-btn ${isActive ? 'active' : ''}" data-month-index="${monthNum}">
                <div class="month-status-dot ${hasText ? 'ready' : ''}"></div>
                <div class="month-name">${name}</div>
            </div>`;
    }).join('');

    grid.querySelectorAll('.month-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            selectedMonthIndex = parseInt(btn.getAttribute('data-month-index'), 10);
            renderGrimFortunesGrid();
            renderSelectedMonthFortune();
        });
    });
}

function renderSelectedMonthFortune() {
    const fortune = fortunesCache.find(f => f.month_index === selectedMonthIndex) || {
        month_index: selectedMonthIndex,
        month_name: MONTH_NAMES[selectedMonthIndex - 1],
        title: `طالع شوم ماه ${MONTH_NAMES[selectedMonthIndex - 1]}`,
        omen_poem: "در این شب سیاهم گم گشت راه مقصود / از گوشه‌ای برون آی ای کوکب هدایت",
        fortune_text: "هنوز برای این ماه طالعی نوشته نشده است. با زدن دکمه «تولید تک‌ماه با AI» آن را ایجاد کنید.",
        doom_level: "شوم"
    };

    document.getElementById('selectedMonthTitle').textContent = fortune.title || `طالع ماه ${MONTH_NAMES[selectedMonthIndex - 1]}`;
    document.getElementById('selectedMonthDoom').textContent = fortune.doom_level || 'شوم';
    document.getElementById('selectedMonthPoem').textContent = `بیت شوم حافظ: « ${fortune.omen_poem || '—'} »`;
    document.getElementById('selectedMonthText').textContent = fortune.fortune_text || '';
}

// ====================================================
// 7. TAB 3: AI STORIES STUDIO (AdminAiStoriesTab)
// ====================================================
function initAiStoriesTab() {
    // Prompt
    const btnToggle = document.getElementById('btnToggleAiPrompt');
    const promptBox = document.getElementById('aiPromptContainer');
    const txtPrompt = document.getElementById('txtAiStoryPrompt');

    txtPrompt.value = localStorage.getItem('HORROR_AI_STORY_PROMPT') || window.DEFAULT_AI_STORY_PROMPT;

    btnToggle.addEventListener('click', () => {
        const isHidden = promptBox.style.display === 'none';
        promptBox.style.display = isHidden ? 'block' : 'none';
        btnToggle.textContent = isHidden ? 'بستن پرامپت' : 'نمایش / ویرایش پرامپت';
    });

    document.getElementById('btnSaveAiPrompt')?.addEventListener('click', async () => {
        const val = txtPrompt.value.trim();
        localStorage.setItem('HORROR_AI_STORY_PROMPT', val);
        try {
            if (SupabaseService.isConfigured()) {
                await SupabaseService.upsertAiPrompt('AI_STORY_PROMPT', val);
            }
            showToast('پرامپت هوش تاریکی در دیتابیس ثبت شد.');
        } catch (e) {
            showToast('پرامپت در حافظه محلی ذخیره شد.');
        }
    });

    document.getElementById('btnResetAiPrompt')?.addEventListener('click', () => {
        txtPrompt.value = window.DEFAULT_AI_STORY_PROMPT;
        localStorage.setItem('HORROR_AI_STORY_PROMPT', window.DEFAULT_AI_STORY_PROMPT);
        showToast('پرامپت هوش تاریکی بازنشانی شد.');
    });

    document.getElementById('btnOpenSqlScript')?.addEventListener('click', () => {
        openModal('modalSqlScript');
    });

    document.getElementById('btnCopySqlScript')?.addEventListener('click', () => {
        const text = document.getElementById('supabaseSqlScriptText').textContent;
        navigator.clipboard.writeText(text);
        showToast('اسکریپت SQL در کلیپ‌بورد کپی شد.');
    });

    // Count selector
    document.querySelectorAll('#aiCountSelector .chip-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('#aiCountSelector .chip-btn').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            selectedAiGenCount = parseInt(btn.getAttribute('data-count'), 10);
            document.getElementById('lblSelectedCount').textContent = selectedAiGenCount;
        });
    });

    // Genre selector
    document.querySelectorAll('#aiGenreSelector .chip-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('#aiGenreSelector .chip-btn').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            selectedAiGenGenre = btn.getAttribute('data-genre');
        });
    });

    // Start Generation Sequence
    document.getElementById('btnStartAiGeneration')?.addEventListener('click', async () => {
        const btn = document.getElementById('btnStartAiGeneration');
        const progressBox = document.getElementById('aiQueueProgressBox');
        const statusText = document.getElementById('aiQueueStatusText');
        const countText = document.getElementById('aiQueueCountText');
        const fillBar = document.getElementById('aiQueueProgressBar');

        btn.disabled = true;
        progressBox.style.display = 'block';

        const total = selectedAiGenCount;
        let completed = 0;
        const blacklist = aiStoriesCache.map(s => s.title);

        for (let i = 1; i <= total; i++) {
            statusText.textContent = `در حال احضار و نگارش داستان ${i} از ${total} (ژانر: ${selectedAiGenGenre})...`;
            countText.textContent = `${completed} / ${total}`;
            fillBar.style.width = `${((i - 1) / total) * 100}%`;

            try {
                const storyData = await GeminiService.generateStory(selectedAiGenGenre, null, blacklist);
                const newStory = {
                    id: `ai-${Date.now()}-${Math.random().toString(36).substr(2, 6)}`,
                    title: storyData.title || 'روایتی از تاریکی',
                    content: storyData.content || '',
                    synopsis: storyData.synopsis || (storyData.content || '').slice(0, 120),
                    genre: storyData.genre || selectedAiGenGenre,
                    cover_image_url: 'https://images.unsplash.com/photo-1509248961158-e54f6934749c?w=500&auto=format&fit=crop&q=80',
                    tags: 'هوش تاریکی',
                    status: 'PUBLISHED',
                    rating: 5.0,
                    rating_count: 1,
                    view_count: 0,
                    created_at: new Date().toISOString()
                };

                if (SupabaseService.isConfigured()) {
                    await SupabaseService.insertAiStory(newStory);
                }

                aiStoriesCache.unshift(newStory);
                blacklist.push(newStory.title);
                completed++;
                renderAiStories();
                renderDashboard();
            } catch (e) {
                console.error(`Error generating story ${i}:`, e);
                statusText.textContent = `خطا در داستان ${i}: ${e.message}`;
            }
        }

        fillBar.style.width = '100%';
        countText.textContent = `${completed} / ${total}`;
        statusText.textContent = `عملیات پایان یافت. ${completed} داستان با موفقیت تولید و منتشر شد.`;
        btn.disabled = false;
        showToast(`${completed} داستان هوش تاریکی با موفقیت ثبت شد.`);
    });

    // SubTabs & Sort
    document.querySelectorAll('[data-ai-status]').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('[data-ai-status]').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            activeAiStatusFilter = btn.getAttribute('data-ai-status');
            renderAiStories();
        });
    });

    document.querySelectorAll('[data-ai-sort]').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('[data-ai-sort]').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            activeAiSortFilter = btn.getAttribute('data-ai-sort');
            renderAiStories();
        });
    });

    // Manual Add AI Story
    document.getElementById('btnOpenManualAddAi')?.addEventListener('click', () => {
        const title = prompt('عنوان داستان هوش مصنوعی:');
        if (!title) return;
        const content = prompt('متن کامل داستان:');
        if (!content) return;

        const manualStory = {
            id: `ai-${Date.now()}`,
            title,
            content,
            synopsis: content.slice(0, 100) + '...',
            genre: 'ماورایی',
            cover_image_url: 'https://images.unsplash.com/photo-1509248961158-e54f6934749c?w=500&auto=format&fit=crop&q=80',
            tags: 'ثبت دستی ادمین',
            status: 'PUBLISHED',
            rating: 5.0,
            view_count: 0
        };

        if (SupabaseService.isConfigured()) {
            SupabaseService.insertAiStory(manualStory);
        }
        aiStoriesCache.unshift(manualStory);
        renderAiStories();
        renderDashboard();
        showToast('داستان دستی ذخیره شد.');
    });

    // Bulk Actions
    document.getElementById('checkSelectAllAi')?.addEventListener('change', (e) => {
        selectedAiStoryIds.clear();
        if (e.target.checked) {
            aiStoriesCache.forEach(s => selectedAiStoryIds.add(s.id));
        }
        updateAiBulkBar();
        renderAiStories();
    });

    document.getElementById('btnBulkPublishAi')?.addEventListener('click', async () => {
        const ids = Array.from(selectedAiStoryIds);
        if (ids.length === 0) return;
        try {
            if (SupabaseService.isConfigured()) {
                await SupabaseService.bulkUpdateAiStoriesStatus(ids, 'PUBLISHED');
            }
            aiStoriesCache.forEach(s => {
                if (ids.includes(s.id)) s.status = 'PUBLISHED';
            });
            selectedAiStoryIds.clear();
            updateAiBulkBar();
            renderAiStories();
            showToast(`${ids.length} داستان منتشر شد.`);
        } catch (e) {
            showToast(`خطا: ${e.message}`, 'error');
        }
    });

    document.getElementById('btnBulkDraftAi')?.addEventListener('click', async () => {
        const ids = Array.from(selectedAiStoryIds);
        if (ids.length === 0) return;
        try {
            if (SupabaseService.isConfigured()) {
                await SupabaseService.bulkUpdateAiStoriesStatus(ids, 'DRAFT');
            }
            aiStoriesCache.forEach(s => {
                if (ids.includes(s.id)) s.status = 'DRAFT';
            });
            selectedAiStoryIds.clear();
            updateAiBulkBar();
            renderAiStories();
            showToast(`${ids.length} داستان به پیش‌نویس تبدیل شد.`);
        } catch (e) {
            showToast(`خطا: ${e.message}`, 'error');
        }
    });

    document.getElementById('btnBulkDeleteAi')?.addEventListener('click', async () => {
        const ids = Array.from(selectedAiStoryIds);
        if (ids.length === 0) return;
        if (!confirm(`آیا از حذف گروهی ${ids.length} داستان هوش مصنوعی اطمینان دارید؟`)) return;

        try {
            if (SupabaseService.isConfigured()) {
                await SupabaseService.bulkDeleteAiStories(ids);
            }
            aiStoriesCache = aiStoriesCache.filter(s => !ids.includes(s.id));
            selectedAiStoryIds.clear();
            updateAiBulkBar();
            renderAiStories();
            renderDashboard();
            showToast(`${ids.length} داستان حذف شد.`);
        } catch (e) {
            showToast(`خطا: ${e.message}`, 'error');
        }
    });
}

function updateAiBulkBar() {
    const bar = document.getElementById('aiBulkBar');
    const lbl = document.getElementById('selectedAiCount');
    const chk = document.getElementById('checkSelectAllAi');

    if (selectedAiStoryIds.size > 0) {
        bar.style.display = 'flex';
        lbl.textContent = selectedAiStoryIds.size;
        chk.checked = selectedAiStoryIds.size === aiStoriesCache.length;
    } else {
        bar.style.display = 'none';
        chk.checked = false;
    }
}

function renderAiStories() {
    const container = document.getElementById('aiStoriesListContainer');

    let filtered = aiStoriesCache.filter(s => {
        return activeAiStatusFilter === 'ALL' || s.status === activeAiStatusFilter;
    });

    // Sorting
    filtered.sort((a, b) => {
        if (activeAiSortFilter === 'NEWEST') return (b.created_at || '').localeCompare(a.created_at || '');
        if (activeAiSortFilter === 'OLDEST') return (a.created_at || '').localeCompare(b.created_at || '');
        if (activeAiSortFilter === 'POPULAR') return (b.view_count || 0) - (a.view_count || 0);
        if (activeAiSortFilter === 'RATING') return (b.rating || 0) - (a.rating || 0);
        return 0;
    });

    if (filtered.length === 0) {
        container.innerHTML = `
            <div style="text-align: center; padding: 40px; color: var(--muted-ash);">
                داستانی در این بخش وجود ندارد.
            </div>`;
        return;
    }

    container.innerHTML = filtered.map(story => {
        const isSelected = selectedAiStoryIds.has(story.id);
        const isPublished = story.status === 'PUBLISHED';

        return `
            <div class="crypt-card ${isSelected ? 'selected' : ''}" style="margin-bottom: 12px; border-color: ${isSelected ? 'var(--blood-glow)' : 'var(--blood-border)'};">
                <div class="card-header-row">
                    <div style="display: flex; align-items: center; gap: 8px;">
                        <input type="checkbox" class="ai-checkbox" data-ai-id="${story.id}" ${isSelected ? 'checked' : ''}>
                        <div>
                            <div class="story-title-text">${story.title}</div>
                            <div class="story-meta-row" style="margin-top: 4px;">
                                <span>ژانر: <strong>${story.genre || 'ماورایی'}</strong></span>
                                <span>امتیاز: ⭐ ${(story.rating || 5).toLocaleString('fa-IR')}</span>
                                <span>بازدید: ${(story.view_count || 0).toLocaleString('fa-IR')}</span>
                            </div>
                        </div>
                    </div>
                    <span class="badge ${isPublished ? 'published' : 'draft'}">
                        ${isPublished ? 'منتشر شده' : 'پیش‌نویس'}
                    </span>
                </div>

                <div class="story-excerpt-text" style="margin: 8px 0;">${story.synopsis || story.content || ''}</div>

                <div style="display: flex; gap: 6px; flex-wrap: wrap;">
                    <button class="btn btn-blood btn-sm btn-read-ai" data-ai-id="${story.id}">مشاهده کامل</button>
                    <button class="btn btn-dark btn-sm btn-toggle-ai-publish" data-ai-id="${story.id}">
                        ${isPublished ? 'پیش‌نویس' : 'انتشار'}
                    </button>
                    <button class="btn btn-outline-blood btn-sm btn-delete-ai" data-ai-id="${story.id}">حذف</button>
                </div>
            </div>`;
    }).join('');

    // Attach Handlers
    container.querySelectorAll('.ai-checkbox').forEach(chk => {
        chk.addEventListener('change', (e) => {
            const id = e.target.getAttribute('data-ai-id');
            if (e.target.checked) selectedAiStoryIds.add(id);
            else selectedAiStoryIds.delete(id);
            updateAiBulkBar();
            renderAiStories();
        });
    });

    container.querySelectorAll('.btn-read-ai').forEach(btn => {
        btn.addEventListener('click', () => {
            const id = btn.getAttribute('data-ai-id');
            const story = aiStoriesCache.find(s => s.id === id);
            if (!story) return;

            document.getElementById('readerTitle').textContent = story.title;
            document.getElementById('readerGenre').textContent = story.genre || 'ماورایی';
            document.getElementById('readerAuthor').textContent = 'هوش تاریکی عمارت وحشت';
            document.getElementById('readerSynopsis').textContent = story.synopsis || '';
            document.getElementById('readerContent').textContent = story.content || '';
            openModal('modalReader');
        });
    });

    container.querySelectorAll('.btn-toggle-ai-publish').forEach(btn => {
        btn.addEventListener('click', async () => {
            const id = btn.getAttribute('data-ai-id');
            const story = aiStoriesCache.find(s => s.id === id);
            if (!story) return;
            const newStatus = story.status === 'PUBLISHED' ? 'DRAFT' : 'PUBLISHED';

            try {
                if (SupabaseService.isConfigured()) {
                    await SupabaseService.updateAiStory(id, { status: newStatus });
                }
                story.status = newStatus;
                renderAiStories();
                showToast(`وضعیت داستان به «${newStatus === 'PUBLISHED' ? 'منتشر شده' : 'پیش‌نویس'}» تغییر کرد.`);
            } catch (e) {
                showToast(`خطا: ${e.message}`, 'error');
            }
        });
    });

    container.querySelectorAll('.btn-delete-ai').forEach(btn => {
        btn.addEventListener('click', async () => {
            const id = btn.getAttribute('data-ai-id');
            const story = aiStoriesCache.find(s => s.id === id);
            if (!story) return;
            if (!confirm(`آیا از حذف داستان «${story.title}» اطمینان دارید؟`)) return;

            try {
                if (SupabaseService.isConfigured()) {
                    await SupabaseService.deleteAiStory(id);
                }
                aiStoriesCache = aiStoriesCache.filter(s => s.id !== id);
                selectedAiStoryIds.delete(id);
                updateAiBulkBar();
                renderAiStories();
                renderDashboard();
                showToast('داستان هوش مصنوعی حذف شد.');
            } catch (e) {
                showToast(`خطا: ${e.message}`, 'error');
            }
        });
    });
}

// ====================================================
// 8. TAB 4: AI & SUPABASE SETTINGS (AdminAiSettingsTab)
// ====================================================
function initAiSettingsTab() {
    // Gemini API Key
    const inputGemini = document.getElementById('inputGeminiKey');
    const btnToggleGemini = document.getElementById('btnToggleGeminiKey');
    inputGemini.value = GeminiService.getApiKey();

    btnToggleGemini.addEventListener('click', () => {
        const isPass = inputGemini.type === 'password';
        inputGemini.type = isPass ? 'text' : 'password';
        btnToggleGemini.textContent = isPass ? '🔒' : '👁️';
    });

    document.getElementById('btnSaveGeminiKey')?.addEventListener('click', () => {
        const key = inputGemini.value.trim();
        GeminiService.setApiKey(key);
        showToast('کلید Gemini API با موفقیت ذخیره شد.');
        checkDiagnostics();
    });

    document.getElementById('btnResetGeminiKey')?.addEventListener('click', () => {
        inputGemini.value = '';
        GeminiService.setApiKey('');
        showToast('کلید Gemini API حذف شد.');
        checkDiagnostics();
    });

    // Render Supported 5 Models
    renderGeminiModelsList();

    // Supabase DB Settings
    const inputUrl = document.getElementById('inputSupabaseUrl');
    const inputKey = document.getElementById('inputSupabaseKey');
    const btnToggleSup = document.getElementById('btnToggleSupabaseKey');

    inputUrl.value = SupabaseService.getUrl();
    inputKey.value = SupabaseService.getAnonKey();

    btnToggleSup.addEventListener('click', () => {
        const isPass = inputKey.type === 'password';
        inputKey.type = isPass ? 'text' : 'password';
        btnToggleSup.textContent = isPass ? '🔒' : '👁️';
    });

    document.getElementById('btnSaveSupabaseConfig')?.addEventListener('click', () => {
        const u = inputUrl.value.trim();
        const k = inputKey.value.trim();
        SupabaseService.saveConfig(u, k);
        showToast('تنظیمات پایگاه داده ذخیره شد.');
        checkDiagnostics();
    });

    document.getElementById('btnTestSupabaseConfig')?.addEventListener('click', async () => {
        try {
            const res = await SupabaseService.testConnection();
            showToast(res);
        } catch (e) {
            showToast(`خطا در اتصال Supabase: ${e.message}`, 'error');
        }
    });

    // In-App Purchase Price
    document.querySelectorAll('[data-preset-price]').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('[data-preset-price]').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            document.getElementById('inputPriceToman').value = btn.getAttribute('data-preset-price');
        });
    });

    document.getElementById('btnSavePrice')?.addEventListener('click', async () => {
        const val = parseInt(document.getElementById('inputPriceToman').value, 10);
        if (!val || isNaN(val)) {
            showToast('لطفاً مبلغ معتبری وارد کنید.', 'error');
            return;
        }

        const btn = document.getElementById('btnSavePrice');
        const status = document.getElementById('priceSaveStatus');
        btn.disabled = true;
        btn.textContent = 'در حال ذخیره در دیتابیس...';

        try {
            if (SupabaseService.isConfigured()) {
                await SupabaseService.updateSubscriptionPrice(val);
            }
            document.getElementById('badgeCurrentPrice').textContent = `${val.toLocaleString('fa-IR')} تومان`;
            status.style.display = 'block';
            status.style.color = 'var(--success-neon)';
            status.textContent = `قیمت ${val.toLocaleString('fa-IR')} تومان با موفقیت در دیتابیس ثبت شد.`;
            showToast('قیمت اشتراک با موفقیت در دیتابیس Supabase ذخیره شد.');
        } catch (e) {
            status.style.display = 'block';
            status.style.color = 'var(--blood-glow)';
            status.textContent = `خطا در ثبت قیمت: ${e.message}`;
            showToast(`خطا در ثبت: ${e.message}`, 'error');
        } finally {
            btn.disabled = false;
            btn.textContent = 'ثبت و به‌روزرسانی قیمت در Supabase';
        }
    });
}

function renderGeminiModelsList() {
    const container = document.getElementById('geminiModelsList');
    const currentModel = GeminiService.getModel();

    container.innerHTML = window.SUPPORTED_GEMINI_MODELS.map(m => {
        const isSelected = m.id === currentModel;
        return `
            <div class="crypt-card-elevated" style="padding: 12px 16px; margin: 0; border-color: ${isSelected ? 'var(--blood-glow)' : 'rgba(138, 125, 147, 0.2)'}; background: ${isSelected ? 'rgba(40, 15, 30, 0.6)' : 'var(--crypt-card-elevated)'};">
                <div style="display: flex; justify-content: space-between; align-items: center; gap: 10px;">
                    <div style="display: flex; align-items: center; gap: 10px; cursor: pointer;" class="model-select-row" data-model-id="${m.id}">
                        <input type="radio" name="gemini_model_choice" value="${m.id}" ${isSelected ? 'checked' : ''}>
                        <div>
                            <div style="font-weight: 700; color: ${isSelected ? 'var(--spectral-white)' : 'var(--muted-ash)'}; font-size: 0.95rem;">${m.id}</div>
                            <div style="font-size: 0.76rem; color: var(--muted-ash);">${m.desc}</div>
                        </div>
                    </div>
                    <button class="btn btn-sm ${isSelected ? 'btn-blood' : 'btn-dark'} btn-test-specific-model" data-model-id="${m.id}">تست مدل</button>
                </div>
            </div>`;
    }).join('');

    container.querySelectorAll('.model-select-row').forEach(row => {
        row.addEventListener('click', () => {
            const id = row.getAttribute('data-model-id');
            GeminiService.setModel(id);
            updateTopBarActiveModel();
            renderGeminiModelsList();
            checkDiagnostics();
            showToast(`مدل فعال هوش تاریکی به «${id}» تغییر یافت.`);
        });
    });

    container.querySelectorAll('.btn-test-specific-model').forEach(btn => {
        btn.addEventListener('click', async () => {
            const id = btn.getAttribute('data-model-id');
            const respCard = document.getElementById('modelTestResponseCard');
            const respText = document.getElementById('modelTestResponseText');

            btn.disabled = true;
            btn.textContent = 'در حال تست...';
            respCard.style.display = 'block';
            respText.textContent = `در حال آزمایش ارتباط با سرور گوگل برای مدل ${id}...`;

            try {
                const res = await GeminiService.testModel(null, id);
                respText.textContent = res;
                showToast(`مدل ${id} با موفقیت تست شد.`);
            } catch (e) {
                respText.textContent = `خطا در ارتباط: ${e.message}`;
                showToast(`خطا در تست: ${e.message}`, 'error');
            } finally {
                btn.disabled = false;
                btn.textContent = 'تست مدل';
            }
        });
    });
}

// ====================================================
// 9. TAB 5: AUTOMATION (AdminAutomationTab)
// ====================================================
function initAutomationTab() {
    document.getElementById('btnSaveAutoGrim')?.addEventListener('click', async () => {
        const enabled = document.getElementById('autoGrimEnabled').checked;
        const hour = parseInt(document.getElementById('autoGrimHour').value, 10);
        try {
            if (SupabaseService.isConfigured()) {
                await SupabaseService.upsertAutomationConfig({
                    task_name: 'AUTO_GRIM_FORTUNES',
                    is_enabled: enabled,
                    execution_hour: hour,
                    updated_at: new Date().toISOString()
                });
            }
            showToast('تنظیمات اتوماسیون طالع شوم با موفقیت ذخیره شد.');
        } catch (e) {
            showToast(`خطا: ${e.message}`, 'error');
        }
    });

    document.getElementById('btnTestRunAutoGrim')?.addEventListener('click', async () => {
        showToast('در حال ارسال درخواست اجرای دستی به سرور...');
        try {
            await SupabaseService.triggerEdgeFunction('auto-grim-fortunes');
            showToast('دستور اجرای خودکار طالع شوم به سرور ارسال شد.');
        } catch (e) {
            showToast(`خطا: ${e.message}`, 'error');
        }
    });

    document.getElementById('btnSaveAutoAi')?.addEventListener('click', async () => {
        const enabled = document.getElementById('autoAiEnabled').checked;
        const count = parseInt(document.getElementById('autoAiCount').value, 10);
        const freq = parseInt(document.getElementById('autoAiFreq').value, 10);
        try {
            if (SupabaseService.isConfigured()) {
                await SupabaseService.upsertAutomationConfig({
                    task_name: 'AUTO_AI_STORIES',
                    is_enabled: enabled,
                    batch_count: count,
                    frequency_per_day: freq,
                    updated_at: new Date().toISOString()
                });
            }
            showToast('تنظیمات اتوماسیون داستان‌های AI ذخیره شد.');
        } catch (e) {
            showToast(`خطا: ${e.message}`, 'error');
        }
    });

    document.getElementById('btnTestRunAutoAi')?.addEventListener('click', async () => {
        showToast('در حال ارسال درخواست تولید خودکار داستان به سرور...');
        try {
            await SupabaseService.triggerEdgeFunction('auto-ai-stories');
            showToast('تولید خودکار داستان بر روی سرور آغاز شد.');
        } catch (e) {
            showToast(`خطا: ${e.message}`, 'error');
        }
    });

    document.getElementById('btnCopyPgCron')?.addEventListener('click', () => {
        const text = document.getElementById('pgCronScriptText').textContent;
        navigator.clipboard.writeText(text);
        showToast('کد pg_cron در کلیپ‌بورد کپی شد.');
    });

    document.getElementById('btnRefreshAutoLogs')?.addEventListener('click', () => {
        loadAllData(true);
    });
}

function renderAutomationLogs() {
    const container = document.getElementById('automationLogsContainer');
    if (!automationLogsCache || automationLogsCache.length === 0) {
        container.innerHTML = `<div style="text-align: center; padding: 20px; color: var(--muted-ash);">هیچ لاگ ثبتی در پایگاه داده یافت نشد.</div>`;
        return;
    }

    container.innerHTML = `
        <div style="overflow-x: auto;">
            <table style="width: 100%; border-collapse: collapse; font-size: 0.8rem; text-align: right;">
                <thead>
                    <tr style="border-bottom: 1px solid rgba(138, 125, 147, 0.2); color: var(--muted-ash);">
                        <th style="padding: 8px;">نام وظیفه</th>
                        <th style="padding: 8px;">نوع تحریک</th>
                        <th style="padding: 8px;">وضعیت</th>
                        <th style="padding: 8px;">تعداد رکورد</th>
                        <th style="padding: 8px;">زمان</th>
                    </tr>
                </thead>
                <tbody>
                    ${automationLogsCache.map(l => `
                        <tr style="border-bottom: 1px solid rgba(138, 125, 147, 0.1);">
                            <td style="padding: 8px; font-weight: 700;">${l.task_name}</td>
                            <td style="padding: 8px;">${l.trigger_type || 'MANUAL'}</td>
                            <td style="padding: 8px;"><span class="badge ${l.status === 'SUCCESS' ? 'published' : 'blood'}">${l.status}</span></td>
                            <td style="padding: 8px;">${l.records_generated || 0}</td>
                            <td style="padding: 8px; color: var(--muted-ash);">${l.created_at ? new Date(l.created_at).toLocaleString('fa-IR') : '—'}</td>
                        </tr>
                    `).join('')}
                </tbody>
            </table>
        </div>`;
}

// ====================================================
// 10. TAB 6: REPORTS (AdminReportsTab)
// ====================================================
function initReportsTab() {
    document.getElementById('btnRefreshReports')?.addEventListener('click', () => {
        loadAllData(true);
    });

    document.getElementById('btnBulkDeleteReports')?.addEventListener('click', async () => {
        const ids = Array.from(selectedReportIds);
        if (ids.length === 0) return;
        if (!confirm(`آیا از حذف ${ids.length} گزارش انتخاب شده اطمینان دارید؟`)) return;

        try {
            if (SupabaseService.isConfigured()) {
                await SupabaseService.bulkDeleteReports(ids);
            }
            reportsCache = reportsCache.filter(r => !ids.includes(r.id));
            selectedReportIds.clear();
            renderReports();
            showToast(`${ids.length} گزارش حذف شد.`);
        } catch (e) {
            showToast(`خطا در حذف: ${e.message}`, 'error');
        }
    });
}

function renderReports() {
    const container = document.getElementById('reportsListContainer');
    const summary = document.getElementById('reportsSummaryCount');
    const btnBulk = document.getElementById('btnBulkDeleteReports');

    summary.textContent = `${reportsCache.length.toLocaleString('fa-IR')} مورد گزارش نامناسب دریافت شده`;

    if (selectedReportIds.size > 0) {
        btnBulk.style.display = 'inline-flex';
        btnBulk.textContent = `حذف گروهی (${selectedReportIds.size})`;
    } else {
        btnBulk.style.display = 'none';
    }

    if (reportsCache.length === 0) {
        container.innerHTML = `
            <div style="text-align: center; padding: 40px; color: var(--muted-ash);">
                <div style="font-size: 2.5rem; margin-bottom: 8px; color: var(--success-neon);">🛡️</div>
                <div style="color: var(--spectral-white); font-weight: 700; margin-bottom: 4px;">هیچ گزارش جدیدی ثبت نشده است.</div>
                <div>تمامی لوح‌ها عاری از هرگونه آلودگی و محتوای نامناسب هستند.</div>
            </div>`;
        return;
    }

    container.innerHTML = reportsCache.map(rep => {
        const isSelected = selectedReportIds.has(rep.id);
        const storyTypeBadge = rep.story_type === 'USER' ? 'روایت کاربر' : (rep.story_type === 'AI' ? 'داستان هوش تاریکی' : 'داستان اصلی');
        const badgeClass = rep.story_type === 'USER' ? 'gold' : (rep.story_type === 'AI' ? 'purple' : 'blood');

        return `
            <div class="crypt-card" style="margin-bottom: 12px; border-color: ${isSelected ? 'var(--blood-glow)' : 'var(--blood-border)'};">
                <div class="card-header-row">
                    <div style="display: flex; align-items: center; gap: 8px;">
                        <input type="checkbox" class="report-checkbox" data-rep-id="${rep.id}" ${isSelected ? 'checked' : ''}>
                        <div>
                            <div class="story-title-text">${rep.story_title || 'عنوان داستان نامشخص'}</div>
                            <div class="story-meta-row" style="margin-top: 4px;">
                                <span>نویسنده/راوی: ${rep.story_author || 'نامشخص'}</span>
                            </div>
                        </div>
                    </div>
                    <span class="badge ${badgeClass}">${storyTypeBadge}</span>
                </div>

                <div style="background: var(--void-black); border-radius: var(--radius-sm); padding: 10px; margin: 8px 0; font-size: 0.85rem; color: #EDE8F5; line-height: 1.5;">
                    علت گزارش: ${rep.reason || 'محتوای نامناسب'}
                </div>

                <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 8px;">
                    <button class="btn btn-dark btn-sm btn-view-report-story" data-story-id="${rep.story_id}" data-story-type="${rep.story_type}">
                        <span>👁️</span> مشاهده داستان مرتبط
                    </button>
                    <button class="btn btn-outline-blood btn-sm btn-dismiss-report" data-rep-id="${rep.id}">
                        رد گزارش (حذف)
                    </button>
                </div>
            </div>`;
    }).join('');

    // Checkboxes
    container.querySelectorAll('.report-checkbox').forEach(chk => {
        chk.addEventListener('change', (e) => {
            const id = e.target.getAttribute('data-rep-id');
            if (e.target.checked) selectedReportIds.add(id);
            else selectedReportIds.delete(id);
            renderReports();
        });
    });

    // View story
    container.querySelectorAll('.btn-view-report-story').forEach(btn => {
        btn.addEventListener('click', () => {
            const sId = btn.getAttribute('data-story-id');
            const sType = btn.getAttribute('data-story-type');

            let found = null;
            if (sType === 'USER') {
                found = submissionsCache.find(s => s.id === sId);
            } else if (sType === 'AI') {
                found = aiStoriesCache.find(s => s.id === sId);
            } else {
                found = realStoriesCache.find(s => s.id === sId);
            }

            if (found) {
                document.getElementById('readerTitle').textContent = found.title;
                document.getElementById('readerGenre').textContent = found.genre || found.tags || 'عمارت وحشت';
                document.getElementById('readerAuthor').textContent = found.author || found.author_name || 'کاتب عمارت';
                document.getElementById('readerSynopsis').textContent = found.synopsis || '';
                document.getElementById('readerContent').textContent = found.content || '';
                openModal('modalReader');
            } else {
                showToast('این داستان در حافظه فعلی یافت نشد.', 'error');
            }
        });
    });

    // Dismiss report
    container.querySelectorAll('.btn-dismiss-report').forEach(btn => {
        btn.addEventListener('click', async () => {
            const id = btn.getAttribute('data-rep-id');
            try {
                if (SupabaseService.isConfigured()) {
                    await SupabaseService.deleteReport(id);
                }
                reportsCache = reportsCache.filter(r => r.id !== id);
                selectedReportIds.delete(id);
                renderReports();
                showToast('گزارش با موفقیت رد شد.');
            } catch (e) {
                showToast(`خطا: ${e.message}`, 'error');
            }
        });
    });
}
