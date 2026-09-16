/**
 * Horror House Web Admin - Global Configuration
 * Allows configuring the Supabase connection globally or dynamically.
 */
window.APP_CONFIG = {
    // Default Supabase project URL and Key (falls back to localStorage if empty)
    SUPABASE_URL: localStorage.getItem('HORROR_SUPABASE_URL') || "",
    SUPABASE_ANON_KEY: localStorage.getItem('HORROR_SUPABASE_KEY') || "",
};
