package com.banking.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.header.ReferrerPolicyServerHttpHeadersWriter;
import org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/actuator/health", "/actuator/info").permitAll()
                        .pathMatchers("/api/auth/**").permitAll()
                        .pathMatchers("/api/**").permitAll()
                        .anyExchange().denyAll()
                )
                .headers(headers -> headers
                        .hsts(hsts -> hsts
                                .maxAge(java.time.Duration.ofDays(365))
                                .includeSubdomains(true)
                        )
                        .frameOptions(fo -> fo.mode(XFrameOptionsServerHttpHeadersWriter.Mode.DENY))
                        .contentTypeOptions(cto -> {})
                        .referrerPolicy(rp -> rp
                                .policy(ReferrerPolicyServerHttpHeadersWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                        .permissionsPolicy(pp -> pp
                                .policy("camera=(), microphone=(), geolocation=(), payment=()"))
                        .cache(ServerHttpSecurity.HeaderSpec.CacheSpec::disable)
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; " +
                                        "img-src 'self' data:; font-src 'self'; frame-ancestors 'none'; " +
                                        "base-uri 'self'; form-action 'self'"))
                )
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:4200",
                "http://localhost:3000"
        ));
        config.setAllowedMethods(List.of(
                HttpMethod.GET.name(), HttpMethod.POST.name(),
                HttpMethod.PUT.name(), HttpMethod.PATCH.name(),
                HttpMethod.DELETE.name(), HttpMethod.OPTIONS.name()
        ));
        config.setAllowedHeaders(List.of(
                "Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With"
        ));
        config.setExposedHeaders(List.of(
                "X-RateLimit-Remaining", "X-RateLimit-Limit", "X-Request-Id"
        ));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
