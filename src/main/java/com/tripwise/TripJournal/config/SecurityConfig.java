package com.tripwise.TripJournal.config;

import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.*;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.env.Environment;
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
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtDecoder jwtDecoder,
                                                   JwtAuthenticationConverter jwtAuthConverter) throws Exception {
        http

                // Stateless API with bearer tokens
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {
                }) // use a CorsConfigurationSource bean if needed
                .sessionManagement(sm -> {
                    sm
                            .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                })

                // AuthZ rules (Authorization)
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                                .requestMatchers(
                                        "/", "/journal/**", "/**/*.css",
                                        "/**/*.js", "/actuator/**").permitAll()
                                .anyRequest().authenticated()

                        // Resource server (JWT)
                ).oauth2ResourceServer(oauth -> oauth
                        // Default JWT decoder is based on spring.security.oauth2.resourceserver.jwt.issuer-uri
                        // You can add a custom converter/decoder here if needed
                        .jwt(
                                jwt -> jwt.decoder(jwtDecoder)
                                        .jwtAuthenticationConverter(jwtAuthConverter))
                );


        // Finalise and return the filter chain
        return http.build(); // returns SecurityFilterChain object and registers it with Spring.
    }

    ;

    /**
     * Map scopes -> authorities and prefer principal = "userId".
     * (We also ensure a "userId" claim exists by copying "sub" if missing — see jwtDecoder().)
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthConverter() {

        var scopeConv = new JwtGrantedAuthoritiesConverter();
        scopeConv.setAuthorityPrefix("ROLE_");
        scopeConv.setAuthoritiesClaimName("authorities");

        var conv = new JwtAuthenticationConverter();
        conv.setJwtGrantedAuthoritiesConverter(scopeConv);
        conv.setPrincipalClaimName("userId");
        return conv;
    }

    /**
     * CORS for your frontend. Adjust origins as needed.
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
    @Bean
    public JwtDecoder jwtDecoder(OAuth2ResourceServerProperties props, Environment env) {
        String jwkSetUri = props.getJwt().getJwkSetUri();
        String issuerUri = props.getJwt().getIssuerUri();

        NimbusJwtDecoder decoder;

        if (jwkSetUri != null && !jwkSetUri.isBlank()) {
            //  Build directly from JWKS (no discovery)
            decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        } else if (issuerUri != null && !issuerUri.isBlank()) {
            // Fallback to issuer (requires OIDC discovery available)
            decoder = (NimbusJwtDecoder) JwtDecoders.fromIssuerLocation(issuerUri);
        } else {
            throw new IllegalStateException("Configure either jwk-set-uri or issuer-uri for the resource server.");
        }

        // ----- Validators -----
        OAuth2TokenValidator<Jwt> base = JwtValidators.createDefault();

        // If you still want issuer claim validation with JWKS, read it from env and add validator.
        String expectedIssuer = env.getProperty("spring.security.oauth2.resourceserver.jwt.issuer-uri");
        if (expectedIssuer != null && !expectedIssuer.isBlank()) {
            base = new DelegatingOAuth2TokenValidator<>(base, JwtValidators.createDefaultWithIssuer(expectedIssuer));
        }

        String audience = env.getProperty("tripjournal.security.audience");
        if (audience != null && !audience.isBlank()) {
            OAuth2TokenValidator<Jwt> audV = token -> {
                List<String> aud = token.getAudience();
                return (aud != null && aud.contains(audience))
                        ? OAuth2TokenValidatorResult.success()
                        : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Invalid audience", null));
            };
            base = new DelegatingOAuth2TokenValidator<>(base, audV);
        }
        decoder.setJwtValidator(base);

        // ----- Claim shaping -----
        Converter<Map<String, Object>, Map<String, Object>> defaults =
                MappedJwtClaimSetConverter.withDefaults(Collections.emptyMap());

        decoder.setClaimSetConverter(in -> {
            Map<String, Object> claims = new HashMap<>(Objects.requireNonNull(defaults.convert(in)));
            claims.putIfAbsent("userId", claims.get("sub"));
            return claims;
        });

        return decoder;
    }
}
