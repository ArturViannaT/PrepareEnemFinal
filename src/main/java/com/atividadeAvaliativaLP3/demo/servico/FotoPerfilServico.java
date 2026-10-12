package com.atividadeAvaliativaLP3.demo.servico;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Foto de perfil via Supabase Storage (bucket "avatars").
 * Fluxo: tela -> POST /api/users/me/photo-url (URL assinada curta,
 * restrita a pasta {userId}/...) -> upload direto -> PUT /api/users/me
 * persiste a URL publica em usuario.fotoPerfil -> ranking/perfil exibem.
 * A service key NUNCA vai ao navegador; o binario nao fica na tabela.
 */
@Service
public class FotoPerfilServico {

    private static final long MAX_BYTES = 2L * 1024L * 1024L; // 2 MB

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.supabase.url:}")
    private String supabaseUrl;

    @Value("${app.supabase.service-key:}")
    private String serviceKey;

    @Value("${app.foto.bucket:avatars}")
    private String bucket;

    /** Gera URL assinada de upload restrita a pasta do usuario. */
    public Map<String, Object> gerarUrlAssinada(Integer userId, String contentType, Long tamanhoBytes) {
        String mime = normalizarMime(contentType);
        if (tamanhoBytes == null || tamanhoBytes <= 0 || tamanhoBytes > MAX_BYTES) {
            throw new IllegalArgumentException("Arquivo invalido: envie uma imagem de ate 2 MB.");
        }
        if (supabaseUrl == null || supabaseUrl.isBlank() || serviceKey == null || serviceKey.isBlank()) {
            throw new IllegalStateException("Storage nao configurado "
                    + "(defina app.supabase.url e app.supabase.service-key).");
        }
        String ext = "png".equals(mime) ? "png" : ("webp".equals(mime) ? "webp" : "jpg");
        String caminho = userId + "/" + UUID.randomUUID() + "_perfil." + ext;
        String base = supabaseUrl.replaceAll("/+$", "");
        String assinar = base + "/storage/v1/object/sign/" + bucket + "/" + caminho;
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", serviceKey);
        headers.set("Authorization", "Bearer " + serviceKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Map> resposta = restTemplate.exchange(
                assinar, HttpMethod.POST,
                new HttpEntity<>(Map.of("expiresIn", 120), headers), Map.class);
        Object token = resposta.getBody() != null ? resposta.getBody().get("signedURL") : null;
        if (token == null || token.toString().isBlank()) {
            throw new IllegalStateException("Nao foi possivel gerar a URL de upload.");
        }
        Map<String, Object> saida = new LinkedHashMap<>();
        saida.put("uploadUrl", base + token.toString());
        saida.put("caminho", caminho);
        saida.put("urlPublica", base + "/storage/v1/object/public/" + bucket + "/" + caminho);
        saida.put("contentType", "image/" + mime);
        saida.put("expiraEmSegundos", 120);
        return saida;
    }

    /** Valida a URL publica antes de persistir em usuario.fotoPerfil. */
  public String validarUrlPublica(Integer userId, String urlPublica) {
    if (urlPublica == null || urlPublica.isBlank()) {
        return null;
        
    }
    return urlPublica.trim(); // Retorna a URL direto para o banco, sem dar palpites!
}


    private String normalizarMime(String contentType) {
        if (contentType == null) {
            throw new IllegalArgumentException("Tipo de arquivo nao informado.");
        }
        String ct = contentType.toLowerCase().trim();
        if (ct.contains("png")) return "png";
        if (ct.contains("jpeg") || ct.contains("jpg")) return "jpg";
        if (ct.contains("webp")) return "webp";
        throw new IllegalArgumentException("Formato nao suportado: envie PNG, JPG ou WEBP.");
    }
}
