// API Helper - Integração Automática entre Frontend, Supabase Auth e Spring Boot API
const API_BASE_URL = (window.location.origin && window.location.origin.startsWith('http') && !window.location.origin.includes('file'))
    ? `${window.location.origin}/api`
    : 'http://localhost:8080/api';

// Faz chamadas HTTP autenticadas para o Spring Boot obtendo o token JWT 100% automaticamente da sessão Supabase Auth
async function apiFetch(endpoint, options = {}) {
    let token = null;
    try {
        token = await getSupabaseToken();
    } catch (e) {
        console.warn('Não foi possível obter token da sessão:', e);
    }
    
    const headers = {
        'Content-Type': 'application/json',
        ...(options.headers || {})
    };

    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    let response;
    try {
        response = await fetch(`${API_BASE_URL}${endpoint}`, {
            ...options,
            headers
        });
    } catch (err) {
        throw new Error('Não foi possível conectar ao servidor. Verifique sua conexão e tente novamente.');
    }

    if (response.status === 401) {
        console.warn('Sessão expirada ou JWT ausente/inválido.');
        if (!window.location.pathname.endsWith('login.html') && !window.location.pathname.endsWith('cadastro.html') && !window.location.pathname.endsWith('index.html')) {
            window.location.href = 'login.html';
        }
    }

    if (!response.ok) {
        let errorMsg = `Erro na requisição (${response.status})`;
        try {
            const errData = await response.json();
            errorMsg = errData.message || (Array.isArray(errData.messages) ? errData.messages.join(', ') : null) || errorMsg;
        } catch (e) {}
        throw new Error(errorMsg);
    }

    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
        return await response.json();
    }
    return await response.text();
}