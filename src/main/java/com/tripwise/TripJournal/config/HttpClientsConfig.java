package com.tripwise.TripJournal.config;

import org.springframework.context.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.client.RestTemplate;

import java.net.http.HttpClient;

/**
 * ================================================================
 * Package Name: com.tripwise.TripJournal.config
 * Author      : Ochwada-GMK
 * Project Name: tripjournal
 * Date        : Friday,  05.Sept.2025 | 11:33
 * Description :
 * ================================================================
 */
@Configuration
public class HttpClientsConfig {

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate rt = new RestTemplate();
        rt.getInterceptors().add((req, body, exec) -> {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth instanceof JwtAuthenticationToken jwt) {
                req.getHeaders().set(HttpHeaders.AUTHORIZATION, "Bearer " + jwt.getToken().getTokenValue());
            }
            return exec.execute(req, body);
        });
        return rt;
    }
}
