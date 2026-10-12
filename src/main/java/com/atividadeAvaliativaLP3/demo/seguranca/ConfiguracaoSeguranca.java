package com.atividadeAvaliativaLP3.demo.seguranca;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class ConfiguracaoSeguranca {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Permitir requisições OPTIONS do browser (preflight CORS)
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // Endpoints públicos (assets e verificação de e-mail no cadastro)
                .requestMatchers("/error", "/api/public/**", "/*.html", "/css/**", "/js/**", "/img/**", "/").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/users/check-email").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/contents", "/api/quizzes", "/api/users/ranking").permitAll()
                // As demais rotas exigem um Bearer Token válido do Supabase Auth
                .anyRequest().authenticated()
            )
            // Habilita validação oficial do JWT do Supabase através das Chaves Públicas (JWKS)
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
