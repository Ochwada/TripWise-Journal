package com.tripwise.TripJournal.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.*;

import java.util.*;

/**
 * ================================================================
 * Package Name: com.tripwise.TripJournal.config
 * Author      : Ochwada-GMK
 * Project Name: tripjournal
 * Date        : Monday,  01.Sept.2025 | 11:39
 * Description :
 * ================================================================
 */
@Configuration
public class SecurityConfig {


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationConverter jwtAuthConverter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // existing public stuff
                        .requestMatchers("/", "/index.html", "/error", "/favicon.ico",
                                "/actuator/health", "/actuator/info",
                                "/swagger-ui/**", "/v3/api-docs/**",
                                "/api/public/**").permitAll()

                        //  ALL static pages & assets under /journal/ are public (GET only)
                        .requestMatchers(HttpMethod.GET, "/journal/**").permitAll()

                        //  APIs remain protected
                        .requestMatchers("/journals/**").authenticated()

                        // anything else → auth
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(o -> o.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter)));
        return http.build();
    };

    /**
     * Map scopes -> authorities and prefer principal = "userId".
     * (We also ensure a "userId" claim exists by copying "sub" if missing — see jwtDecoder().)
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthConverter() {
        var scopeConv = new JwtGrantedAuthoritiesConverter();
        scopeConv.setAuthorityPrefix("ROLE_");          // optional
        scopeConv.setAuthoritiesClaimName("authorities"); // optional; Google ID tokens won’t have this

        var conv = new JwtAuthenticationConverter();
        conv.setJwtGrantedAuthoritiesConverter(scopeConv);
        conv.setPrincipalClaimName("userId");
        return conv;
    }

    /**
     * CORS for the  frontend.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        var cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of("http://localhost:3000"));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(true);

        var src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", cfg);
        return src;
    }


    /**
     * Build JwtDecoder from JWK Set URI if provided; otherwise fall back to issuer.
     * Also adds optional audience validation and ensures a "userId" claim (fallback to "sub").
     * <p>
     * Supported properties:
     * - spring.security.oauth2.resourceserver.jwt.jwk-set-uri
     * - spring.security.oauth2.resourceserver.jwt.issuer-uri   (fallback)
     * - tripjournal.security.audience                          (optional)
     */
    /** Validate Google issuer + audience; also copy "sub" → "userId" so principal is stable. */
    @Bean
    public JwtDecoder jwtDecoder(
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuer,
            @Value("${google.client-id}") String audience) {

        NimbusJwtDecoder decoder = JwtDecoders.fromIssuerLocation(issuer);

        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);
        OAuth2TokenValidator<Jwt> withAudience = jwt ->
                (jwt.getAudience() != null && jwt.getAudience().contains(audience))
                        ? OAuth2TokenValidatorResult.success()
                        : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Wrong audience", null));

        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(withIssuer, withAudience));

        // copy "sub" => "userId" if missing
        decoder.setClaimSetConverter(claims -> {
            var m = new java.util.HashMap<>(claims);
            m.putIfAbsent("userId", m.get("sub"));
            return m;
        });

        return decoder;
    }
}
