package com.tripwise.TripJournal.controller;


import org.springframework.security.core.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * ================================================================
 * Package Name: com.tripwise.TripJournal.controller
 * Author      : Ochwada-GMK
 * Project Name: tripjournal
 * Date        : Monday,  01.Sept.2025 | 11:04
 * Description :
 * ================================================================
 */
@Component
public class ControllerHelpers {
    public String resolveUserId(Authentication auth) {
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();

            String userId = jwt.getClaimAsString("userId");

            if (userId == null || userId.isEmpty()) {
                userId = jwt.getSubject();
            }
            return userId;
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetails ud) {
            return ud.getUsername();
        }
        return auth.getName();
    }

    @SuppressWarnings("unused")
    public Collection<? extends GrantedAuthority> roles(Authentication auth) {
        return auth.getAuthorities();
    }
}
