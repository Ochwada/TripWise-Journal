package com.tripwise.TripJournal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.client.RestTemplate;

/**
 * ================================================================
 * Package Name: com.tripwise.TripJournal.config
 * Author      : Ochwada-GMK
 * Project Name: tripjournal
 * Date        : Thursday,  04.Sept.2025 | 12:28
 * Description : Registers a {@link RestTemplate} bean preconfigured with a request interceptor that propagates the
 *  caller's OAuth2 access token to downstream services.
 * ================================================================
 */
@Configuration
public class RestTemplateConfig {
    /**
     * Creates a {@link RestTemplate} with an interceptor that forwards the current request's JWT as a Bearer token to
     * downstream HTTP calls.
     *
     * @return a configured, reusable {@link RestTemplate} bean
     */
    @Bean
    public RestTemplate restTemplate() {
// Create the RestTemplate instance used across the app
        RestTemplate restTemplate = new RestTemplate();

        // Interceptor to propagate Authorization: Bearer <token>
        ClientHttpRequestInterceptor interceptor = (req, body, exec) -> {
            // Grab the current Authentication from Spring Security context
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            // If the user is authenticated with a Jwt, forward it as Bearer token
            if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
                // Equivalent to: req.getHeaders().add("Authorization", "Bearer " + token)
                req.getHeaders().setBearerAuth(jwt.getTokenValue());
            }
            // Continue the request chain (execute the HTTP call)
            return exec.execute(req, body);
        };
        // Attach our interceptor to this RestTemplate
        restTemplate.getInterceptors().add(interceptor);
        // Return the configured bean to the application context
        return restTemplate;
    }
}
