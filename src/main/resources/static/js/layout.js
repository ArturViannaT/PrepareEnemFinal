/* Layout compartilhado: sidebar, marca e barra do usuario. */
const LAYOUT_LINKS = [
    { href: 'dashboard.html', icone: 'fa-house', texto: 'Inicio' },
    { href: 'quizzes.html', icone: 'fa-gamepad', texto: 'Quizzes' },
    { href: 'conteudos.html', icone: 'fa-book', texto: 'Materiais' },
    { href: 'perfil.html', icone: 'fa-trophy', texto: 'Ranking' },
    { href: 'minha-conta.html', icone: 'fa-user-gear', texto: 'Minha Conta' }
];

let usuarioLayout = null;

function criarAvatarHTML(usuario, classeExtra) {
    const classe = 'avatar ' + (classeExtra || '');
    if (usuario && usuario.fotoPerfil) {
        return '<span class="' + classe + '"><img src="' + usuario.fotoPerfil + '" alt="Foto de perfil"></span>';
    }
    const inicial = (usuario && usuario.nome) ? usuario.nome.trim().charAt(0) : '?';
    return '<span class="' + classe + '">' + inicial + '</span>';
}

function paginaAtual() {
    const partes = window.location.pathname.split('/');
    return partes[partes.length - 1] || 'dashboard.html';
}

function montarSidebar(usuario, ehProfessor) {
    const nav = document.getElementById('appSidebar') || document.querySelector('nav.sidebar');
    if (!nav) return;
    const atual = paginaAtual();
    const links = LAYOUT_LINKS.map(function(item) {
        const ativo = item.href === atual ? ' class="active"' : '';
        return '<li><a href="' + item.href + '"' + ativo + '><i class="fas ' + item.icone + '"></i> ' + item.texto + '</a></li>';
    }).join('');
    const nome = usuario ? usuario.nome : 'Visitante';
    const papel = ehProfessor ? 'Professor' : 'Aluno';
    nav.innerHTML =
        '<a class="logo brand brand--sidebar" href="dashboard.html" title="Prepare Enem">' +
        '<img src="img/logo.png" alt="Logo Prepare Enem">' +
        '<span class="logo-name brand-nome">Prepare <span>Enem</span></span></a>' +
        '<ul class="nav-links">' + links +
        '<li><a href="#" onclick="fazerLogoutSupabase(); return false;"><i class="fas fa-right-from-bracket"></i> Sair</a></li></ul>' +
        '<div style="margin-top:auto;display:flex;align-items:center;gap:0.65rem;padding-top:1.5rem;border-top:1px solid var(--border);">' +
        criarAvatarHTML(usuario, 'avatar--sm') +
        '<div style="min-width:0;"><div style="font-weight:700;font-size:0.9rem;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">' +
        nome + '</div><div style="font-size:0.78rem;color:var(--text-muted);">' + papel + '</div></div></div>';
}

function preencherUsuarioNoHeader(usuario) {
    if (!usuario) return;
    const pontosEl = document.getElementById('userPoints');
    const nivelEl = document.getElementById('userLevel');
    const avatarEl = document.getElementById('userAvatar') || document.querySelector('.conteudoPrincipal .user-status .fotoperfil');
    if (usuario.tipo === 'PROFESSOR') {
        if (pontosEl) pontosEl.textContent = 'Professor';
        if (nivelEl) nivelEl.textContent = 'Verificado';
    } else {
        if (pontosEl) pontosEl.textContent = Math.round(usuario.pontos || 0) + ' pts';
        if (nivelEl) nivelEl.textContent = 'Nivel ' + (usuario.nivel || 1);
    }
    if (avatarEl) {
        avatarEl.innerHTML = usuario.fotoPerfil
            ? '<img src="' + usuario.fotoPerfil + '" alt="Foto de perfil" style="width:100%;height:100%;object-fit:cover;border-radius:50%;">'
            : '<i class="fas fa-user"></i>';
    }
    const nomeEl = document.getElementById('userNameHeader');
    if (nomeEl) nomeEl.textContent = usuario.nome;
}

function montarMenuMobile() {
    if (document.querySelector('.menu-toggle')) return;
    const botao = document.createElement('button');
    botao.className = 'menu-toggle';
    botao.type = 'button';
    botao.setAttribute('aria-label', 'Abrir menu');
    botao.innerHTML = '<i class="fas fa-bars"></i>';
    botao.addEventListener('click', function() {
        const nav = document.getElementById('appSidebar') || document.querySelector('nav.sidebar');
        if (nav) nav.classList.toggle('aberta');
    });
    document.body.appendChild(botao);
}

async function montarLayout(usuarioConhecido) {
    try {
        let usuario = usuarioConhecido || null;
        if (!usuario) {
            if (typeof getCurrentSession === 'function') {
                const session = await getCurrentSession();
                if (!session) {
                    window.location.href = 'login.html';
                    return null;
                }
            }
            usuario = await apiFetch('/users/me');
        }
        usuarioLayout = usuario;
        const ehProfessor = usuario && usuario.tipo === 'PROFESSOR';
        montarSidebar(usuario, ehProfessor);
        montarMenuMobile();
        preencherUsuarioNoHeader(usuario);
        return usuario;
    } catch (erro) {
        console.error('Falha ao montar o layout da pagina:', erro);
        montarSidebar(null, false);
        montarMenuMobile();
        const mensagem = document.getElementById('mensagemLayout');
        if (mensagem) {
            mensagem.className = 'message-error';
            mensagem.textContent = 'Nao foi possivel carregar seus dados (' + erro.message + '). Verifique se o backend esta em execucao.';
        }
        return null;
    }
}

function mostrarMensagem(idElemento, texto, tipo) {
    const el = document.getElementById(idElemento);
    if (!el) {
        if (tipo === 'error') alert(texto);
        return;
    }
    el.className = tipo === 'success' ? 'message-success' : (tipo === 'warning' ? 'message-warning' : 'message-error');
    el.textContent = texto;
    el.style.display = 'block';
}

document.addEventListener('DOMContentLoaded', function() {
    if (!document.body.dataset.layoutManual) {
        montarLayout();
    }
});
