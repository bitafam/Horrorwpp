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
let crashLogsCache = [];
let heartbeatsCache = [];
let subscribersCache = [];

let selectedRealStoryIds = new Set();
let selectedAiStoryIds = new Set();
let selectedReportIds = new Set();

let activeRealStatusFilter = 'ALL';
let activeSubStatusFilter = 'ALL';
let activeAiStatusFilter = 'ALL';
let activeAiSortFilter = 'NEWEST';
let activeCrashFilter = 'ALL';

let selectedMonthIndex = 1; // 1 to 12
let selectedAiGenCount = 1;
let selectedAiGenGenre = 'ماورایی';

const MONTH_NAMES = [
    "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
    "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
];

// Pure Gothic and Horror local drawable identifiers
const HORROR_POSTERS = [
    "poster_dark_demon",
    "poster_ghost_corridor",
    "poster_blood_ritual",
    "poster_screaming_wraith",
    "poster_cemetery_curse",
    "universal_horror",
    "nightmare_crypt",
    "haunted_chamber",
    "img_ai_story_poster_1",
    "img_ai_story_poster_2",
    "img_poster_1",
    "img_poster_2",
    "img_poster_3"
];

function getRandomHorrorPoster(storyId) {
    if (storyId) {
        let hash = 0;
        for (let i = 0; i < storyId.length; i++) {
            hash = ((hash << 5) - hash) + storyId.charCodeAt(i);
            hash |= 0;
        }
        const index = Math.abs(hash) % HORROR_POSTERS.length;
        return HORROR_POSTERS[index];
    }
    return HORROR_POSTERS[Math.floor(Math.random() * HORROR_POSTERS.length)];
}

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
    initLogsTab();

    if (SupabaseService.isLoggedIn() || SupabaseService.isConfigured()) {
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
function updateLoginDbStatus() {
    const statusText = document.getElementById('loginDbStatusText');
    const dbConnectForm = document.getElementById('dbConnectForm');
    const toggleDbBtn = document.getElementById('btnToggleDbConfig');
    if (!statusText) return;

    if (SupabaseService.isConfigured()) {
        statusText.innerHTML = '<span style="color: var(--success-neon);">🟢 دیتابیس Supabase تنظیم شده است</span>';
        if (dbConnectForm) dbConnectForm.style.display = 'none';
        if (toggleDbBtn) toggleDbBtn.textContent = '⚙️ تغییر آدرس/کلید دیتابیس';
    } else {
        statusText.innerHTML = '<span style="color: #ff6b6b;">🔴 دیتابیس تنظیم نشده است</span>';
        if (dbConnectForm) dbConnectForm.style.display = 'block';
        if (toggleDbBtn) toggleDbBtn.textContent = 'بستن تنظیمات دیتابیس';
    }
}

function initAuth() {
    // Fill existing Supabase URL/Key if saved
    const inputLoginUrl = document.getElementById('loginSupabaseUrl');
    const inputLoginKey = document.getElementById('loginSupabaseKey');
    if (inputLoginUrl) inputLoginUrl.value = SupabaseService.getUrl();
    if (inputLoginKey) inputLoginKey.value = SupabaseService.getAnonKey();

    updateLoginDbStatus();

    // Toggle DB key visibility
    document.getElementById('btnToggleLoginDbKey')?.addEventListener('click', () => {
        if (!inputLoginKey) return;
        const isPass = inputLoginKey.type === 'password';
        inputLoginKey.type = isPass ? 'text' : 'password';
        document.getElementById('btnToggleLoginDbKey').textContent = isPass ? '🔒' : '👁️';
    });

    // Toggle Login Password visibility
    const passInput = document.getElementById('loginPassword');
    document.getElementById('btnToggleLoginPass')?.addEventListener('click', () => {
        if (!passInput) return;
        const isPass = passInput.type === 'password';
        passInput.type = isPass ? 'text' : 'password';
        document.getElementById('btnToggleLoginPass').textContent = isPass ? '🔒' : '👁️';
    });

    // Toggle DB Config Form
    const toggleDbBtn = document.getElementById('btnToggleDbConfig');
    const dbConnectForm = document.getElementById('dbConnectForm');
    toggleDbBtn?.addEventListener('click', () => {
        const isHidden = dbConnectForm.style.display === 'none';
        dbConnectForm.style.display = isHidden ? 'block' : 'none';
        toggleDbBtn.textContent = isHidden ? 'بستن تنظیمات دیتابیس' : '⚙️ تغییر آدرس/کلید دیتابیس';
    });

    // Primary Admin Email & Password Login
    document.getElementById('btnLoginSubmit')?.addEventListener('click', async () => {
        const email = (document.getElementById('loginEmail')?.value || '').trim();
        const pass = (document.getElementById('loginPassword')?.value || '').trim();
        const feedback = document.getElementById('loginFeedback');
        const btn = document.getElementById('btnLoginSubmit');

        // Check if DB URL & Key inputs exist and are populated
        const enteredUrl = (inputLoginUrl?.value || '').trim();
        const enteredKey = (inputLoginKey?.value || '').trim();
        if (enteredUrl && enteredKey) {
            SupabaseService.saveConfig(enteredUrl, enteredKey);
        }

        if (!email || !pass) {
            feedback.className = 'login-feedback error';
            feedback.textContent = 'لطفاً ایمیل و رمز عبور ادمین را وارد نمایید.';
            return;
        }

        if (!SupabaseService.isConfigured()) {
            feedback.className = 'login-feedback error';
            feedback.textContent = 'لطفاً آدرس و کلید Supabase را وارد کنید تا پنل به دیتابیس متصل شود.';
            if (dbConnectForm) dbConnectForm.style.display = 'block';
            return;
        }

        btn.disabled = true;
        btn.innerHTML = '<span>در حال بررسی اعتبار و اتصال به دیتابیس...</span>';
        feedback.className = 'login-feedback';
        feedback.style.display = 'none';

        try {
            await SupabaseService.login(email, pass);
            localStorage.setItem('HORROR_IS_LOGGED_IN', 'true');
            localStorage.setItem('HORROR_ADMIN_EMAIL', email);
            feedback.className = 'login-feedback success';
            feedback.textContent = 'ورود به عنوان ادمین عمارت وحشت با موفقیت انجام شد.';
            setTimeout(() => {
                showAdminPanel();
                loadAllData();
            }, 300);
        } catch (err) {
            feedback.className = 'login-feedback error';
            feedback.textContent = err.message || 'خطا در ورود یا اتصال به دیتابیس.';
        } finally {
            btn.disabled = false;
            btn.innerHTML = '<span>ورود به پنل مدیریت</span>';
        }
    });

    // Top Bar Logout
    document.getElementById('btnTopLogout')?.addEventListener('click', () => {
        if (confirm('آیا از خروج از پنل مدیریت اطمینان دارید؟')) {
            SupabaseService.logout();
            showLoginScreen();
            updateLoginDbStatus();
            showToast('با موفقیت از پنل مدیریت خارج شدید.');
        }
    });

    // Top Bar Refresh
    document.getElementById('btnTopRefresh')?.addEventListener('click', () => {
        loadAllData(true);
    });

    // Top Bar DB Status button -> Open Database Connection Modal
    document.getElementById('btnTopDbStatus')?.addEventListener('click', () => {
        openDbConnectionModal();
    });

    // Init DB Connection Modal listeners
    initDbConnectionModal();
}

function initDbConnectionModal() {
    const modalKeyInput = document.getElementById('modalInputDbKey');
    document.getElementById('btnToggleModalDbKey')?.addEventListener('click', () => {
        if (!modalKeyInput) return;
        const isPass = modalKeyInput.type === 'password';
        modalKeyInput.type = isPass ? 'text' : 'password';
        document.getElementById('btnToggleModalDbKey').textContent = isPass ? '🔒' : '👁️';
    });

    document.getElementById('btnModalOpenSqlScript')?.addEventListener('click', () => {
        closeModal('modalDatabaseConnection');
        openModal('modalSqlScript');
    });

    document.getElementById('btnModalTestDb')?.addEventListener('click', async () => {
        const url = (document.getElementById('modalInputDbUrl')?.value || '').trim();
        const key = (document.getElementById('modalInputDbKey')?.value || '').trim();
        if (url && key) {
            SupabaseService.saveConfig(url, key);
        }
        await refreshDbModalStatus();
        updateTopBarDbStatus();
    });

    document.getElementById('btnModalSaveDb')?.addEventListener('click', async () => {
        const url = (document.getElementById('modalInputDbUrl')?.value || '').trim();
        const key = (document.getElementById('modalInputDbKey')?.value || '').trim();
        if (!url || !key) {
            showToast('لطفاً آدرس و کلید Supabase را کامل وارد کنید.', 'error');
            return;
        }
        SupabaseService.saveConfig(url, key);
        showToast('تنظیمات پایگاه داده ذخیره شد. در حال برقراری ارتباط...');
        await refreshDbModalStatus();
        updateTopBarDbStatus();
        closeModal('modalDatabaseConnection');
        await checkDiagnostics();
        await loadAllData(true);
    });
}

function openDbConnectionModal() {
    const urlInput = document.getElementById('modalInputDbUrl');
    const keyInput = document.getElementById('modalInputDbKey');
    if (urlInput) urlInput.value = SupabaseService.getUrl();
    if (keyInput) keyInput.value = SupabaseService.getAnonKey();

    refreshDbModalStatus();
    openModal('modalDatabaseConnection');
}

async function refreshDbModalStatus() {
    const icon = document.getElementById('modalDbStatusIcon');
    const title = document.getElementById('modalDbStatusTitle');
    const desc = document.getElementById('modalDbStatusDesc');
    const alertBox = document.getElementById('modalDbStatusAlert');

    if (!SupabaseService.isConfigured()) {
        if (icon) icon.textContent = '🔴';
        if (title) title.textContent = 'پایگاه داده Supabase تنظیم نشده است';
        if (desc) desc.textContent = 'لطفاً آدرس پروژه (URL) و کلید دسترسی (Anon Key) را در کادرهای زیر وارد کنید.';
        if (alertBox) {
            alertBox.style.borderColor = 'var(--blood-glow)';
            alertBox.style.background = 'rgba(180, 20, 20, 0.15)';
        }
        return;
    }

    if (icon) icon.textContent = '⏳';
    if (title) title.textContent = 'در حال بررسی اتصال به دیتابیس...';
    if (desc) desc.textContent = 'لطفاً چند لحظه صبر کنید...';

    try {
        const msg = await SupabaseService.testConnection();
        if (icon) icon.textContent = '🟢';
        if (title) title.textContent = 'اتصال با پایگاه داده برقرار است';
        if (desc) desc.textContent = msg;
        if (alertBox) {
            alertBox.style.borderColor = 'rgba(0, 230, 118, 0.4)';
            alertBox.style.background = 'rgba(0, 230, 118, 0.1)';
        }
    } catch (e) {
        if (icon) icon.textContent = '🔴';
        if (title) title.textContent = 'خطا در اتصال به پایگاه داده';
        if (desc) desc.textContent = e.message;
        if (alertBox) {
            alertBox.style.borderColor = 'var(--blood-glow)';
            alertBox.style.background = 'rgba(180, 20, 20, 0.15)';
        }
    }
}

function showLoginScreen() {
    document.getElementById('loginScreenContainer').style.display = 'flex';
    document.getElementById('adminPanelContainer').style.display = 'none';
    updateLoginDbStatus();
}

function showAdminPanel() {
    document.getElementById('loginScreenContainer').style.display = 'none';
    document.getElementById('adminPanelContainer').style.display = 'flex';
    updateTopBarActiveModel();
    updateTopBarDbStatus();
}

function updateTopBarActiveModel() {
    const el = document.getElementById('topBarActiveModel');
    if (el) el.textContent = GeminiService.getModel();
}

function updateTopBarDbStatus() {
    const ind = document.getElementById('topDbIndicator');
    const txt = document.getElementById('topDbStatusText');
    if (!ind || !txt) return;

    if (SupabaseService.isConfigured() && SupabaseService.lastConnected !== false) {
        ind.textContent = '🟢';
        txt.textContent = 'دیتابیس متصل';
    } else {
        ind.textContent = '🔴';
        txt.textContent = 'دیتابیس نامتصل';
    }
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
// 3. LOAD ALL DATA (Live Supabase Sync & Zero Fake Data)
// ====================================================
async function loadAllData(showToastMsg = false) {
    if (showToastMsg) showToast('در حال همگام‌سازی اطلاعات با پایگاه داده...');

    // 1. Diagnostics
    await checkDiagnostics();

    // 2. Sync App Settings (Gemini API Key, Active Model, etc.) from Supabase
    try {
        if (SupabaseService.isConfigured()) {
            const settings = await SupabaseService.getAppSettings();
            if (settings && Array.isArray(settings)) {
                const geminiKeyRow = settings.find(s => s.key === 'GEMINI_API_KEY');
                if (geminiKeyRow && geminiKeyRow.value && geminiKeyRow.value.trim()) {
                    GeminiService.setApiKey(geminiKeyRow.value.trim());
                    const inputGem = document.getElementById('inputGeminiKey');
                    if (inputGem) inputGem.value = geminiKeyRow.value.trim();
                }

                const geminiModelRow = settings.find(s => s.key === 'GEMINI_MODEL');
                if (geminiModelRow && geminiModelRow.value && geminiModelRow.value.trim()) {
                    GeminiService.setModel(geminiModelRow.value.trim());
                    updateTopBarActiveModel();
                    renderGeminiModelsList();
                }
            }
        }
    } catch (e) {
        console.warn('App settings sync warning:', e);
    }

    // 3. Real Stories (Strictly from Database)
    try {
        const stories = await SupabaseService.getRealStories();
        realStoriesCache = Array.isArray(stories) ? stories : [];
    } catch (e) {
        console.warn('Real stories error:', e);
        realStoriesCache = [];
    }

    // 4. Submissions
    try {
        const subs = await SupabaseService.getSubmissions();
        submissionsCache = Array.isArray(subs) ? subs : [];
    } catch (e) {
        console.warn('Submissions error:', e);
        submissionsCache = [];
    }

    // 5. Fortunes (Strictly from Database)
    try {
        const forts = await SupabaseService.getGrimFortunes();
        fortunesCache = Array.isArray(forts) ? forts : [];
    } catch (e) {
        console.warn('Fortunes error:', e);
        fortunesCache = [];
    }

    // 6. AI Stories (Strictly from Database)
    try {
        const ai = await SupabaseService.getAiStories();
        aiStoriesCache = Array.isArray(ai) ? ai : [];
    } catch (e) {
        console.warn('AI stories error:', e);
        aiStoriesCache = [];
    }

    // 7. Subscription Price
    try {
        const price = await SupabaseService.getSubscriptionPrice();
        const badgePrice = document.getElementById('badgeCurrentPrice');
        const inputPrice = document.getElementById('inputPriceToman');
        if (badgePrice) badgePrice.textContent = `${price.toLocaleString('fa-IR')} تومان`;
        if (inputPrice) inputPrice.value = price;
    } catch (e) {
        console.warn('Price warning:', e);
    }

    // 8. Reports
    try {
        const reports = await SupabaseService.getReports();
        reportsCache = Array.isArray(reports) ? reports : [];
    } catch (e) {
        console.warn('Reports warning:', e);
        reportsCache = [];
    }

    // 9. Automation logs
    try {
        const logs = await SupabaseService.getAutomationLogs();
        automationLogsCache = Array.isArray(logs) ? logs : [];
    } catch (e) {
        console.warn('Automation logs warning:', e);
        automationLogsCache = [];
    }

    // 10. Crash Logs
    try {
        const crashes = await SupabaseService.getCrashLogs();
        crashLogsCache = Array.isArray(crashes) ? crashes : [];
    } catch (e) {
        console.warn('Crash logs error:', e);
        crashLogsCache = [];
    }

    // 11. User Heartbeats & Online Status
    try {
        const beats = await SupabaseService.getHeartbeats();
        heartbeatsCache = Array.isArray(beats) ? beats : [];
    } catch (e) {
        console.warn('Heartbeats error:', e);
        heartbeatsCache = [];
    }

    // 12. VIP Subscribers
    try {
        const subs = await SupabaseService.getSubscribers();
        subscribersCache = Array.isArray(subs) ? subs : [];
    } catch (e) {
        console.warn('Subscribers error:', e);
        subscribersCache = [];
    }

    // Re-check diagnostics after settings sync
    await checkDiagnostics();

    // Re-render UI views
    renderDashboard();
    renderRealStories();
    renderSubmissions();
    renderGrimFortunesGrid();
    renderSelectedMonthFortune();
    renderAiStories();
    renderReports();
    renderAutomationLogs();
    renderLogsAndTelemetry();

    if (showToastMsg) showToast('اطلاعات با موفقیت از پایگاه داده همگام شدند.');
}

async function checkDiagnostics() {
    const badgeDb = document.getElementById('statusBadgeSupabase');
    const badgeAi = document.getElementById('statusBadgeGemini');

    if (SupabaseService.isConfigured()) {
        try {
            await SupabaseService.testConnection();
            if (badgeDb) {
                badgeDb.className = 'badge published';
                badgeDb.textContent = 'پایگاه داده: متصل (Supabase)';
            }
        } catch (e) {
            if (badgeDb) {
                badgeDb.className = 'badge draft';
                badgeDb.textContent = 'پایگاه داده: خطا در برقراری ارتباط';
            }
        }
    } else {
        if (badgeDb) {
            badgeDb.className = 'badge blood';
            badgeDb.textContent = 'پایگاه داده: پیکربندی نشده';
        }
    }

    if (GeminiService.getApiKey()) {
        if (badgeAi) {
            badgeAi.className = 'badge published';
            badgeAi.textContent = `هوش مصنوعی: آماده (${GeminiService.getModel()})`;
        }
    } else {
        if (badgeAi) {
            badgeAi.className = 'badge blood';
            badgeAi.textContent = 'هوش مصنوعی: کلید در دیتابیس ثبت نشده';
        }
    }

    updateTopBarDbStatus();
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

    // Render DB Alert if disconnected
    const alertContainer = document.getElementById('dashboardDbAlert');
    if (alertContainer) {
        if (!SupabaseService.isConfigured() || SupabaseService.lastConnected === false) {
            alertContainer.innerHTML = `
                <div class="crypt-card" style="border: 1px solid var(--blood-glow); background: rgba(180, 20, 20, 0.15); margin-bottom: 16px; padding: 14px 18px;">
                    <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px;">
                        <div style="display: flex; align-items: center; gap: 10px;">
                            <span style="font-size: 1.6rem;">⚠️</span>
                            <div>
                                <strong style="color: #ff6b6b; font-size: 0.95rem;">ارتباط با پایگاه داده Supabase برقرار نیست</strong>
                                <div style="color: var(--muted-ash); font-size: 0.8rem; margin-top: 2px;">
                                    دسترسی به اطلاعات زنده داستان‌ها، طالع‌ها و کاربران قطع شده است. برای برقراری مجدد ارتباط کلیک کنید.
                                </div>
                            </div>
                        </div>
                        <button class="btn btn-blood btn-sm" id="btnDashboardConnectDb">
                            <span>🔌</span> بررسی و اتصال مجدد
                        </button>
                    </div>
                </div>
            `;
            document.getElementById('btnDashboardConnectDb')?.addEventListener('click', () => {
                openDbConnectionModal();
            });
        } else {
            alertContainer.innerHTML = '';
        }
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
        document.getElementById('formStoryOriginSubId').value = '';
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
        const rawId = document.getElementById('formStoryId').value.trim();
        const isEdit = Boolean(rawId);
        const id = isEdit ? rawId : (window.crypto && crypto.randomUUID ? crypto.randomUUID() : `real-${Date.now()}`);
        const title = document.getElementById('formStoryTitle').value.trim();
        const author = document.getElementById('formStoryAuthor').value.trim() || 'کاتب عمارت';
        const source = document.getElementById('formStorySource').value.trim() || 'روایات واقعی';
        const cover = document.getElementById('formStoryCover').value.trim() || getRandomHorrorPoster(id);
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
                if (isEdit) {
                    await SupabaseService.updateRealStory(id, storyObj);
                } else {
                    await SupabaseService.insertRealStory(storyObj);
                }
            }
            // Update local cache
            const existingIdx = realStoriesCache.findIndex(s => s.id === id);
            if (existingIdx >= 0) {
                realStoriesCache[existingIdx] = { ...realStoriesCache[existingIdx], ...storyObj };
            } else {
                realStoriesCache.unshift(storyObj);
            }

            // If this story originated from a user submission, update the submission status to PUBLISHED
            const originSubId = document.getElementById('formStoryOriginSubId')?.value.trim();
            if (originSubId) {
                try {
                    if (SupabaseService.isConfigured()) {
                        await SupabaseService.updateSubmission(originSubId, { status: 'PUBLISHED' });
                    }
                    const sub = submissionsCache.find(s => s.id === originSubId);
                    if (sub) sub.status = 'PUBLISHED';
                    renderSubmissions();
                } catch (subErr) {
                    console.warn('Could not update origin submission status:', subErr);
                }
            }

            closeModal('modalStoryForm');
            renderRealStories();
            renderDashboard();
            showToast('داستان واقعی با موفقیت در دیتابیس منتشر و ثبت شد.');
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
            const storyId = `real-${Date.now()}-${Math.random().toString(36).substr(2, 6)}`;
            list.push({
                id: storyId,
                title,
                content,
                author,
                source,
                cover_image_url: poster || getRandomHorrorPoster(storyId),
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
        const coverUrl = story.cover_image_url || getRandomHorrorPoster(story.id);

        return `
            <div class="story-card-item ${isSelected ? 'selected' : ''}" data-story-id="${story.id}">
                <div style="display: flex; align-items: center; padding-left: 4px;">
                    <input type="checkbox" class="story-checkbox" data-story-id="${story.id}" ${isSelected ? 'checked' : ''}>
                </div>
                <div class="story-poster-box">
                    <img src="${coverUrl}" class="story-poster-img" alt="${story.title}" onerror="this.src='${getRandomHorrorPoster(story.id)}'">
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
        const isPublished = s.status === 'PUBLISHED' || s.status === 'APPROVED';
        let matchesStatus = activeSubStatusFilter === 'ALL';
        if (activeSubStatusFilter === 'PENDING') matchesStatus = s.status === 'PENDING';
        else if (activeSubStatusFilter === 'PUBLISHED' || activeSubStatusFilter === 'APPROVED') matchesStatus = isPublished;
        else if (activeSubStatusFilter === 'REJECTED') matchesStatus = s.status === 'REJECTED';

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
        const isPublished = sub.status === 'PUBLISHED' || sub.status === 'APPROVED';
        const statusBadgeClass = isPublished ? 'published' : (sub.status === 'REJECTED' ? 'blood' : 'draft');
        const statusText = isPublished ? 'تأیید و منتشر شده' : (sub.status === 'REJECTED' ? 'رد شده' : 'در انتظار بررسی');

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

            const newRealId = window.crypto && crypto.randomUUID ? crypto.randomUUID() : `real-${Date.now()}`;
            document.getElementById('formStoryId').value = newRealId;
            document.getElementById('formStoryOriginSubId').value = sub.id;
            document.getElementById('modalStoryTitle').textContent = 'انتشار روایت کاربر در داستان‌های واقعی';
            document.getElementById('formStoryTitle').value = sub.title || '';
            document.getElementById('formStoryAuthor').value = sub.author_name || 'راوی عمارت';
            document.getElementById('formStorySource').value = 'ارسالی کاربران';
            document.getElementById('formStoryCover').value = getRandomHorrorPoster(sub.id);
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
                    await SupabaseService.updateSubmission(id, { status: 'PUBLISHED' });
                }
                const sub = submissionsCache.find(s => s.id === id);
                if (sub) sub.status = 'PUBLISHED';
                renderSubmissions();
                renderDashboard();
                showToast('روایت کاربر تأیید و در برنامه منتشر شد.');
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
شعر: [بیت شعر شوم سروده جادوگر شرور]
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
        title: `طالع ماه ${MONTH_NAMES[selectedMonthIndex - 1]} (در دیتابیس ثبت نشده)`,
        omen_poem: "—",
        fortune_text: "هنوز برای این ماه طالعی در پایگاه داده ذخیره نشده است. با زدن دکمه «تولید تک‌ماه با هوش تاریکی» یا «تولید همزمان ۱۲ ماه» در بالای همین صفحه، طالع این ماه مستقیماً با هوش مصنوعی احضار و در پایگاه داده ثبت می‌شود.",
        doom_level: "نامشخص"
    };

    document.getElementById('selectedMonthTitle').textContent = fortune.title || `طالع ماه ${MONTH_NAMES[selectedMonthIndex - 1]}`;
    document.getElementById('selectedMonthDoom').textContent = fortune.doom_level || 'نامشخص';
    document.getElementById('selectedMonthPoem').textContent = `بیت شوم جادوگر شرور: « ${fortune.omen_poem || '—'} »`;
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
                const aiStoryId = `ai-${Date.now()}-${Math.random().toString(36).substr(2, 6)}`;
                const newStory = {
                    id: aiStoryId,
                    title: storyData.title || 'روایتی از تاریکی',
                    content: storyData.content || '',
                    synopsis: storyData.synopsis || (storyData.content || '').slice(0, 120),
                    genre: storyData.genre || selectedAiGenGenre,
                    cover_image_url: getRandomHorrorPoster(aiStoryId),
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

        const mId = `ai-${Date.now()}`;
        const manualStory = {
            id: mId,
            title,
            content,
            synopsis: content.slice(0, 100) + '...',
            genre: 'ماورایی',
            cover_image_url: getRandomHorrorPoster(mId),
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

    document.getElementById('btnSaveGeminiKey')?.addEventListener('click', async () => {
        const key = inputGemini.value.trim();
        GeminiService.setApiKey(key);
        try {
            if (SupabaseService.isConfigured()) {
                await SupabaseService.upsertAppSetting('GEMINI_API_KEY', key, 'Google AI Studio Gemini API Key');
            }
            showToast('کلید Gemini API با موفقیت در پنل و پایگاه داده ذخیره شد.');
        } catch (e) {
            showToast(`کلید ذخیره شد (خطای همگام‌سازی دیتابیس: ${e.message})`);
        }
        await checkDiagnostics();
    });

    document.getElementById('btnResetGeminiKey')?.addEventListener('click', async () => {
        inputGemini.value = '';
        GeminiService.setApiKey('');
        try {
            if (SupabaseService.isConfigured()) {
                await SupabaseService.upsertAppSetting('GEMINI_API_KEY', '', 'Google AI Studio Gemini API Key');
            }
            showToast('کلید Gemini API حذف شد.');
        } catch (e) {
            showToast('کلید در پنل حذف شد.');
        }
        await checkDiagnostics();
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

    document.getElementById('btnSaveSupabaseConfig')?.addEventListener('click', async () => {
        const u = inputUrl.value.trim();
        const k = inputKey.value.trim();
        SupabaseService.saveConfig(u, k);
        showToast('تنظیمات پایگاه داده ذخیره شد. در حال همگام‌سازی با سرور...');
        await checkDiagnostics();
        await loadAllData(true);
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
        row.addEventListener('click', async () => {
            const id = row.getAttribute('data-model-id');
            GeminiService.setModel(id);
            try {
                if (SupabaseService.isConfigured()) {
                    await SupabaseService.upsertAppSetting('GEMINI_MODEL', id, 'Active Gemini Model');
                }
            } catch (e) {
                console.warn('Could not save model to database:', e);
            }
            updateTopBarActiveModel();
            renderGeminiModelsList();
            await checkDiagnostics();
            showToast(`مدل فعال هوش تاریکی به «${id}» تغییر یافت و در دیتابیس ثبت شد.`);
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

// ====================================================
// 8. LOGS & TELEMETRY CONTROLLER
// ====================================================
function initLogsTab() {
    // Crash Filter Buttons
    document.querySelectorAll('[data-crash-filter]').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('[data-crash-filter]').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            activeCrashFilter = btn.getAttribute('data-crash-filter');
            renderLogsAndTelemetry();
        });
    });

    // Refresh Crash Logs
    document.getElementById('btnRefreshCrashLogs')?.addEventListener('click', async () => {
        try {
            const crashes = await SupabaseService.getCrashLogs();
            crashLogsCache = Array.isArray(crashes) ? crashes : [];
            renderLogsAndTelemetry();
            showToast('گزارش‌های خرابی به‌روزرسانی شدند.');
        } catch (e) {
            showToast(`خطا در بازیابی لاگ‌ها: ${e.message}`, 'error');
        }
    });

    // Simulate Test Crash
    document.getElementById('btnSimulateCrash')?.addEventListener('click', async () => {
        try {
            await SupabaseService.logCrash({
                error_message: 'NullPointerException: Attempt to invoke virtual method on a null object reference (Simulated Diagnostic Crash)',
                stack_trace: 'at com.example.horrorhouse.ui.reader.ReaderScreenKt.ReaderContent(ReaderScreen.kt:142)\n' +
                             'at com.example.horrorhouse.ui.HorrorAppKt$HorrorNavHost$1$3.invoke(HorrorApp.kt:89)',
                device_model: 'Samsung Galaxy S24 Ultra',
                android_version: 'Android 14 (API 34)',
                app_version: '1.2.0'
            });
            const crashes = await SupabaseService.getCrashLogs();
            crashLogsCache = Array.isArray(crashes) ? crashes : [];
            renderLogsAndTelemetry();
            showToast('خرابی آزمایشی با موفقیت ثبت شد.');
        } catch (e) {
            showToast(`خطا در ثبت لاگ آزمایشی: ${e.message}`, 'error');
        }
    });

    // Clear Resolved Crashes
    document.getElementById('btnClearResolvedCrashes')?.addEventListener('click', async () => {
        if (!confirm('آیا از پاکسازی تمام خرابی‌های برطرف‌شده اطمینان دارید؟')) return;
        try {
            await SupabaseService.clearResolvedCrashes();
            crashLogsCache = crashLogsCache.filter(c => !c.is_resolved);
            renderLogsAndTelemetry();
            showToast('خرابی‌های برطرف‌شده پاکسازی شدند.');
        } catch (e) {
            showToast(`خطا: ${e.message}`, 'error');
        }
    });

    // Refresh Subscribers
    document.getElementById('btnRefreshSubscribers')?.addEventListener('click', async () => {
        try {
            const subs = await SupabaseService.getSubscribers();
            subscribersCache = Array.isArray(subs) ? subs : [];
            renderLogsAndTelemetry();
            showToast('لیست مشترکین ویژه به‌روز شد.');
        } catch (e) {
            showToast(`خطا: ${e.message}`, 'error');
        }
    });
}

function renderLogsAndTelemetry() {
    const isCrashResolved = (c) => c.status === 'RESOLVED' || Boolean(c.is_resolved);

    // 1. KPI Metrics
    const unresolvedCrashes = crashLogsCache.filter(c => !isCrashResolved(c));
    const resolvedCrashes = crashLogsCache.filter(c => isCrashResolved(c));

    const elCrashCount = document.getElementById('telemetryCrashCount');
    if (elCrashCount) elCrashCount.textContent = unresolvedCrashes.length.toLocaleString('fa-IR');

    const elCrashRate = document.getElementById('telemetryCrashRate');
    if (elCrashRate) {
        if (unresolvedCrashes.length === 0) {
            elCrashRate.textContent = '۱۰۰٪ پایدار';
        } else {
            elCrashRate.textContent = `${unresolvedCrashes.length} مورد فعال`;
        }
    }

    // Deduplicate heartbeats strictly by device_id
    const uniqueDevicesMap = new Map();
    for (const h of heartbeatsCache) {
        const dId = (h.device_id || h.user_id || '').trim();
        if (!dId) continue;
        const curTime = new Date(h.last_seen_at || h.last_seen || h.updated_at || h.created_at || 0).getTime();
        if (uniqueDevicesMap.has(dId)) {
            const ex = uniqueDevicesMap.get(dId);
            const exTime = new Date(ex.last_seen_at || ex.last_seen || ex.updated_at || ex.created_at || 0).getTime();
            if (curTime > exTime) uniqueDevicesMap.set(dId, h);
        } else {
            uniqueDevicesMap.set(dId, h);
        }
    }
    const dedupedHeartbeats = Array.from(uniqueDevicesMap.values());

    // Five minutes threshold for online users
    const fiveMinutesAgo = new Date(Date.now() - 5 * 60 * 1000).toISOString();
    const onlineUsers = dedupedHeartbeats.filter(h => {
        const lastSeen = h.last_seen_at || h.last_seen || h.updated_at || h.created_at;
        return (lastSeen && lastSeen >= fiveMinutesAgo) || Boolean(h.is_online);
    });
    const elOnline = document.getElementById('telemetryOnlineCount');
    if (elOnline) elOnline.textContent = (onlineUsers.length || 0).toLocaleString('fa-IR');

    // 24 hours active users
    const oneDayAgo = new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString();
    const activeDaily = dedupedHeartbeats.filter(h => {
        const lastSeen = h.last_seen_at || h.last_seen || h.updated_at || h.created_at;
        return (lastSeen && lastSeen >= oneDayAgo);
    });
    const elActiveDaily = document.getElementById('telemetryActiveDailyCount');
    if (elActiveDaily) elActiveDaily.textContent = (activeDaily.length || dedupedHeartbeats.length || 0).toLocaleString('fa-IR');

    // Subscribers count (strict 1 per device)
    const activeSubs = subscribersCache.filter(s => s.status === 'ACTIVE' || s.is_active || s.is_subscribed === true);
    const elSubs = document.getElementById('telemetrySubscribersCount');
    if (elSubs) elSubs.textContent = activeSubs.length.toLocaleString('fa-IR');

    // Navbar Badge
    const navBadge = document.getElementById('navCrashesBadge');
    if (navBadge) {
        if (unresolvedCrashes.length > 0) {
            navBadge.style.display = 'inline-block';
            navBadge.textContent = unresolvedCrashes.length.toLocaleString('fa-IR');
        } else {
            navBadge.style.display = 'none';
        }
    }

    // 2. Render Crash Logs
    const crashContainer = document.getElementById('crashLogsListContainer');
    if (crashContainer) {
        let filteredCrashes = crashLogsCache.filter(c => {
            if (activeCrashFilter === 'UNRESOLVED') return !isCrashResolved(c);
            if (activeCrashFilter === 'RESOLVED') return isCrashResolved(c);
            return true;
        });

        if (filteredCrashes.length === 0) {
            crashContainer.innerHTML = `
                <div style="text-align: center; padding: 32px; color: var(--muted-ash);">
                    <div style="font-size: 2rem; margin-bottom: 8px;">🛡️</div>
                    <div style="color: var(--spectral-white); font-weight: bold;">هیچ خطای ثبتی در این بخش وجود ندارد</div>
                    <div style="font-size: 0.85rem; margin-top: 4px;">برنامه کامپایل شده در بهترین حالت پایداری اجرا می‌شود.</div>
                </div>`;
        } else {
            crashContainer.innerHTML = filteredCrashes.map(c => {
                const dateStr = c.created_at ? new Date(c.created_at).toLocaleString('fa-IR') : 'نامشخص';
                const isResolved = isCrashResolved(c);

                return `
                    <div class="crypt-card" style="margin-bottom: 12px; border-color: ${isResolved ? 'rgba(70,205,120,0.3)' : 'var(--blood-border)'};">
                        <div class="card-header-row">
                            <div style="display: flex; align-items: center; gap: 8px;">
                                <span style="font-size: 1.2rem;">${isResolved ? '✅' : '💥'}</span>
                                <div>
                                    <div class="story-title-text" style="color: ${isResolved ? '#9be7b5' : 'var(--blood-red)'}; font-size: 0.95rem; direction: ltr; text-align: right;">
                                        ${c.error_message || 'خطای ناشناخته برنامه'}
                                    </div>
                                    <div class="story-meta-row" style="margin-top: 4px;">
                                        <span>📱 دستگاه: ${c.device_model || 'دستگاه نامشخص'}</span>
                                        <span>⚙️ اندروید: ${c.android_version || 'نامشخص'}</span>
                                        <span>📦 نسخه اپ: ${c.app_version || '1.0.0'}</span>
                                        <span>🕒 زمان: ${dateStr}</span>
                                    </div>
                                </div>
                            </div>
                            <span class="badge ${isResolved ? 'published' : 'blood'}">
                                ${isResolved ? 'برطرف شده' : 'نیاز به بررسی'}
                            </span>
                        </div>

                        ${c.stack_trace ? `
                            <details style="margin: 10px 0; background: var(--void-black); border-radius: 6px; padding: 8px 12px; border: 1px solid var(--blood-border);">
                                <summary style="cursor: pointer; color: var(--muted-ash); font-size: 0.8rem; outline: none;">
                                    مشاهده ردپای خطا (Stack Trace)
                                </summary>
                                <pre style="margin-top: 8px; font-family: monospace; font-size: 0.75rem; color: #ffb8b8; white-space: pre-wrap; direction: ltr; max-height: 180px; overflow-y: auto;">${c.stack_trace}</pre>
                            </details>
                        ` : ''}

                        <div style="display: flex; justify-content: flex-end; gap: 8px; margin-top: 8px;">
                            ${!isResolved ? `
                                <button class="btn btn-dark btn-sm btn-resolve-crash" data-crash-id="${c.id}">
                                    <span>✅</span> علامت‌گذاری به عنوان حل‌شده
                                </button>
                            ` : `
                                <span style="font-size: 0.8rem; color: var(--success-neon); align-self: center;">برطرف شد</span>
                            `}
                        </div>
                    </div>`;
            }).join('');

            // Attach resolve handlers
            crashContainer.querySelectorAll('.btn-resolve-crash').forEach(btn => {
                btn.addEventListener('click', async () => {
                    const cId = btn.getAttribute('data-crash-id');
                    try {
                        await SupabaseService.resolveCrash(cId);
                        const found = crashLogsCache.find(c => c.id === cId);
                        if (found) {
                            found.status = 'RESOLVED';
                            found.is_resolved = true;
                        }
                        renderLogsAndTelemetry();
                        showToast('خرابی به عنوان برطرف‌شده ثبت گردید.');
                    } catch (e) {
                        showToast(`خطا: ${e.message}`, 'error');
                    }
                });
            });
        }
    }

    // 3. Render Online Users & Heartbeats
    const heartbeatsContainer = document.getElementById('heartbeatsListContainer');
    if (heartbeatsContainer) {
        if (heartbeatsCache.length === 0) {
            heartbeatsContainer.innerHTML = `
                <div style="text-align: center; padding: 24px; color: var(--muted-ash); font-size: 0.9rem;">
                    هیچ داده‌ای از وضعیت کاربران در دیتابیس دریافت نشده است.
                </div>`;
        } else {
            heartbeatsContainer.innerHTML = heartbeatsCache.slice(0, 25).map(h => {
                const lastSeenRaw = h.last_seen_at || h.last_seen || h.updated_at || h.created_at;
                const dateStr = lastSeenRaw ? new Date(lastSeenRaw).toLocaleTimeString('fa-IR') : '—';
                const isOnline = (lastSeenRaw && lastSeenRaw >= fiveMinutesAgo) || Boolean(h.is_online);
                const isVip = Boolean(h.is_subscribed);
                return `
                    <div style="display: flex; justify-content: space-between; align-items: center; padding: 10px 14px; background: var(--obsidian-surface); border-bottom: 1px solid var(--blood-border); border-radius: var(--radius-sm); margin-bottom: 6px;">
                        <div style="display: flex; align-items: center; gap: 10px;">
                            <span style="display: inline-block; width: 10px; height: 10px; border-radius: 50%; background: ${isOnline ? '#00e676' : '#666'}; box-shadow: ${isOnline ? '0 0 8px #00e676' : 'none'};"></span>
                            <div>
                                <div style="font-weight: 600; font-size: 0.85rem; color: var(--spectral-white);">
                                    ${h.user_email || `کاربر ${h.device_id ? h.device_id.slice(0, 8) : (h.user_id ? h.user_id.slice(0, 8) : 'عمارت')}`}
                                    ${isVip ? '<span style="color: #ffd700; margin-right: 4px; font-size: 0.75rem;">👑 ویژه</span>' : ''}
                                </div>
                                <div style="font-size: 0.75rem; color: var(--muted-ash); margin-top: 2px;">
                                    ${h.device_model ? `دستگاه: ${h.device_model} • ` : ''}طرح: ${h.subscription_plan || (isVip ? 'دائمی' : 'عادی')}
                                </div>
                            </div>
                        </div>
                        <div style="text-align: left; font-size: 0.8rem; color: var(--muted-ash);">
                            <div>${isOnline ? '<span style="color: #00e676; font-weight: bold;">آنلاین</span>' : 'آفلاین'}</div>
                            <div style="font-size: 0.7rem; margin-top: 2px;">${dateStr}</div>
                        </div>
                    </div>`;
            }).join('');
        }
    }

    // 4. Render Subscribers List
    const subsContainer = document.getElementById('subscribersListContainer');
    if (subsContainer) {
        if (subscribersCache.length === 0) {
            subsContainer.innerHTML = `
                <div style="text-align: center; padding: 24px; color: var(--muted-ash); font-size: 0.9rem;">
                    هنوز اشتراک ویژه‌ای ثبت نگردیده است.
                </div>`;
        } else {
            subsContainer.innerHTML = subscribersCache.slice(0, 25).map(s => {
                const isVip = s.status === 'ACTIVE' || Boolean(s.is_active) || s.is_subscribed === true;
                const createdDate = s.created_at || s.last_seen_at || s.last_seen || s.updated_at;
                const dateStr = createdDate ? new Date(createdDate).toLocaleDateString('fa-IR') : 'نامشخص';
                const subId = (s.device_id || s.id || '').trim();
                return `
                    <div style="display: flex; justify-content: space-between; align-items: center; padding: 10px 14px; background: var(--obsidian-surface); border-bottom: 1px solid var(--blood-border); border-radius: var(--radius-sm); margin-bottom: 6px;">
                        <div style="display: flex; align-items: center; gap: 8px;">
                            <span>👑</span>
                            <div>
                                <div style="font-weight: 600; font-size: 0.85rem; color: #ffd700;">${s.user_email || `مشترک ${s.device_id ? s.device_id.slice(0, 8) : (s.user_id ? s.user_id.slice(0, 8) : 'ویژه')}`}</div>
                                <div style="font-size: 0.75rem; color: var(--muted-ash); margin-top: 2px;">
                                    ${s.device_model ? `دستگاه: ${s.device_model} • ` : ''}طرح: ${s.subscription_plan || s.plan_name || 'عضویت دائمی عمارت'}
                                </div>
                            </div>
                        </div>
                        <div style="display: flex; align-items: center; gap: 10px;">
                            <div style="text-align: left;">
                                <span class="badge ${isVip ? 'gold' : 'draft'}">${isVip ? 'فعال' : 'منقضی'}</span>
                                <div style="font-size: 0.7rem; color: var(--muted-ash); margin-top: 4px;">${dateStr}</div>
                            </div>
                            ${subId ? `
                            <button onclick="handleDeleteSubscriber('${subId}')" title="حذف رکورد" style="background: none; border: none; color: var(--blood-crimson); cursor: pointer; font-size: 1rem; padding: 4px 6px;">
                                🗑️
                            </button>` : ''}
                        </div>
                    </div>`;
            }).join('');
        }
    }
}

async function handleDeleteSubscriber(id) {
    if (!confirm('آیا از حذف این رکورد اشتراک اطمینان دارید؟')) return;
    try {
        await SupabaseService.deleteSubscriberHeartbeat(id);
        showToast('رکورد اشتراک با موفقیت حذف گردید.');
        await syncDataFromSupabase(false);
    } catch (e) {
        console.error('Delete subscriber error:', e);
        showToast('خطا در حذف رکورد اشتراک');
    }
}

async function handleCleanupDuplicateSubscribers() {
    try {
        const removed = await SupabaseService.cleanupDuplicateHeartbeats();
        showToast(`پاکسازی انجام شد (${removed} رکورد تکراری حذف گردید).`);
        await syncDataFromSupabase(false);
    } catch (e) {
        showToast('خطا در پاکسازی تکراری‌ها');
    }
}


