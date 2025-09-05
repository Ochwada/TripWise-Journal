package com.tripwise.TripJournal.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * ================================================================
 * Package Name: com.tripwise.TripJournal.service
 * Author      : Ochwada-GMK
 * Project Name: tripjournal
 * Date        : Thursday,  04.Sept.2025 | 16:56
 * Description :
 * ================================================================
 */
@Service
public class TokenService {
    private final byte[] secret;

    public TokenService(@Value("${JWT_SECRET}") String secret) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8); // 32+ bytes
    }

    public String mint(String userId) throws JOSEException {
        var claims = new JWTClaimsSet.Builder()
                .subject(userId)
                .claim("userId", userId)
                .audience("tripjournal")     // optional but nice
                .issueTime(new Date())
                .expirationTime(Date.from(Instant.now().plusSeconds(3600)))
                .build();

        var header = new JWSHeader.Builder(JWSAlgorithm.HS256)
                .type(JOSEObjectType.JWT).build();

        var jwt = new SignedJWT(header, claims);
        jwt.sign(new MACSigner(secret));
        return jwt.serialize();
    }
}
