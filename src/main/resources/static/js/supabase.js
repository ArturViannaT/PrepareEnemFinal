// Supabase Auth Integration - PrepareENEM Frontend
const SUPABASE_URL = 'https://pleflxgjytiyzzfaxfum.supabase.co';
const SUPABASE_ANON_KEY = 'sb_publishable_URYPLutgHzmarz0coHf2AA_BLOqtETo';
let supabaseClient = null;

/**
 * Obtém ou inicializa o cliente Supabase
 */
function getSupabaseClient() {
    if (!supabaseClient && window.supabase) {
        supabaseClient = window.supabase.createClient(SUPABASE_URL, SUPABASE_ANON_KEY);
    }
    return supabaseClient;
}

/**
 * Obtém o access_token/JWT atual da sessão do Supabase Auth
 */
async function getSupabaseToken() {
    const client = getSupabaseClient();
    if (!client) return null;
    const { data: { session } } = await client.auth.getSession();
    return session ? session.access_token : null;
}

/**
 * Obtém a sessão atual do usuário
 */
async function getCurrentSession() {
    const client = getSupabaseClient();
    if (!client) return null;
    const { data: { session } } = await client.auth.getSession();
    return session;
}

/**
 * Verifica se o usuário está autenticado
 */
async function isUserAuthenticated() {
    const session = await getCurrentSession();
    return !!session;
}

/**
 * Obtém informações do usuário atual
 */
async function getCurrentUser() {
    const client = getSupabaseClient();
    if (!client) return null;
    const { data: { user } } = await client.auth.getUser();
    return user;
}

/**
 * Verifica se o e-mail foi confirmado no Supabase
 */
async function isEmailConfirmed() {
    const user = await getCurrentUser();
    if (!user) return false;
    return Boolean(user.email_confirmed_at || user.confirmed_at);
}

/**
 * Realiza o logout automático
 */
async function fazerLogoutSupabase() {
    const client = getSupabaseClient();
    if (client) {
        await client.auth.signOut();
    }
    window.location.href = 'login.html';
}

/**
 * Processa erros de autenticação com mensagens amigáveis
 */
function getAuthErrorMessage(error) {
    if (!error || !error.message) {
        return 'Erro desconhecido. Tente novamente.';
    }

    const message = error.message.toLowerCase();

    if (message.includes('already registered')) {
        return 'Este e-mail já está registrado. Faça login com suas credenciais.';
    }
    if (message.includes('weak password')) {
        return 'Senha fraca. Use pelo menos 6 caracteres com letras e números.';
    }
    if (message.includes('invalid email')) {
        return 'E-mail inválido. Por favor, verifique.';
    }
    if (message.includes('invalid login credentials') || message.includes('invalid login')) {
        return 'E-mail ou senha incorretos. Tente novamente.';
    }
    if (message.includes('email not confirmed')) {
        return 'E-mail não foi confirmado. Verifique sua caixa de entrada e clique no link de confirmação.';
    }
    if (message.includes('network error') || message.includes('failed to fetch')) {
        return 'Erro de conexão. Verifique sua internet e tente novamente.';
    }

    return error.message;
}