package com.tripwise.TripJournal.config;

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
                                                   JwtDecoder jwtDecoder) throws Exception {
        http

                // Stateless API with bearer tokens
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {
                }) // use a CorsConfigurationSource bean if needed
                .sessionManagement(sm -> {
                    sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                })


                // AuthZ rules (Authorization)
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                                .anyRequest()
                                .authenticated()

                        // Resource server (JWT)
                ).oauth2ResourceServer(o -> o
                        .jwt(jwtConfigurer -> {
                            // Default JWT decoder is based on spring.security.oauth2.resourceserver.jwt.issuer-uri
                            // You can add a custom converter/decoder here if needed
                        })
                );
        // Finalise and return the filter chain
        return http.build(); // returns SecurityFilterChain object and registers it with Spring.
    };

    /**
     * Map scopes -> authorities and prefer principal = "userId".
     * (We also ensure a "userId" claim exists by copying "sub" if missing — see jwtDecoder().)
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthConverter() {
        JwtGrantedAuthoritiesConverter scopeConverter = new JwtGrantedAuthoritiesConverter();
        scopeConverter.setAuthorityPrefix("ROLE_");
        scopeConverter.setAuthoritiesClaimName("authorities");

        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(scopeConverter);
        jwtConverter.setPrincipalClaimName("userId");

        return jwtConverter;
    }

    /**
     * CORS for your frontend. Adjust origins as needed.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*")); // includes Authorization
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }


    /**
     * Issuer-based JwtDecoder with:
     *  - Default issuer validation (required)
     *  - Optional audience validation (if tripjournal.security.audience is set)
     *  - Claim shaping: ensure "userId" exists (fallback to "sub")
     *
     * Requires:
     *   spring.security.oauth2.resourceserver.jwt.issuer-uri=https://<issuer>/
     */
    @Bean
    public JwtDecoder jwtDecoder(
            org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties props,
            Environment env
    ) {
        String issuer = props.getJwt().getIssuerUri();

        // Spring returns a NimbusJwtDecoder here; cast to customize
        NimbusJwtDecoder decoder = (NimbusJwtDecoder) JwtDecoders.fromIssuerLocation(issuer);

        // ----- Validators -----
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);

        String audience = env.getProperty("tripjournal.security.audience");
        if (audience != null && !audience.isBlank()) {
            OAuth2TokenValidator<Jwt> audienceValidator = token -> {
                List<String> aud = token.getAudience();
                return (aud != null && aud.contains(audience))
                        ? OAuth2TokenValidatorResult.success()
                        : OAuth2TokenValidatorResult.failure(
                        new OAuth2Error("invalid_token", "Invalid audience", null)
                );
            };
            decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator));
        } else {
            decoder.setJwtValidator(withIssuer);
        }

        // ----- Claim shaping -----
        // Guarantee a "userId" claim (used as principal) by copying from "sub" if absent.
        Converter<Map<String, Object>, Map<String, Object>> defaults =
                org.springframework.security.oauth2.jwt.MappedJwtClaimSetConverter.withDefaults(Collections.emptyMap());

        decoder.setClaimSetConverter(claimsIn -> {
            Map<String, Object> claims = new HashMap<>(Objects.requireNonNull(defaults.convert(claimsIn)));
            claims.putIfAbsent("userId", claims.get("sub"));
            return claims;
        });

        return decoder;
    }

}
