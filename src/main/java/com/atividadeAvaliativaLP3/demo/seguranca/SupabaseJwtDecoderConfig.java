package com.atividadeAvaliativaLP3.demo.seguranca;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;

import java.util.List;

/**
 * Decodificador/validador dos JWT emitidos pelo Supabase Auth.
 *
 * A verificação é FEITA DE VERDADE, contra as chaves públicas oficiais do projeto (JWKS):
 *   1. assinatura conferida com a chave pública publicada em
 *      `{supabase}/auth/v1/.well-known/jwks.json` (algoritmo assimétrico, ex.: ES256);
 *   2. `exp`/`nbf` validados;
 *   3. `iss` conferido com o issuer do projeto configurado em `app.supabase.issuer`.
 *
 * Não existe geração de token nem login por senha no backend: o Spring Boot apenas
 * VALIDA o token entregue pelo Supabase Auth.
 */
@Configuration
public class SupabaseJwtDecoderConfig {

    @Bean
    public JwtDecoder jwtDecoder(
            @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}") String jwkSetUri,
            @Value("${app.supabase.issuer:}") String issuer) {

        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri)
                .jwsAlgorithms(algorithms -> {
                    // Supabase publica chaves assimétricas (P-256/ES256 e RSA/RS256).
                    algorithms.add(SignatureAlgorithm.ES256);
                    algorithms.add(SignatureAlgorithm.RS256);
                })
                .build();

        OAuth2TokenValidator<Jwt> comTempo = JwtValidators.createDefault();
        if (issuer != null && !issuer.isBlank()) {
            OAuth2TokenValidator<Jwt> comIssuer = JwtValidators.createDefaultWithIssuer(issuer);
            decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(List.of(comTempo, comIssuer)));
        } else {
            decoder.setJwtValidator(comTempo);
        }

        return decoder;
    }
}
