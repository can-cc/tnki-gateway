package com.tnki.gateway;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AuthServiceTest {

    @Value("${jwt.token.expire.time}")
    private long tokenExpireTime;

    @Autowired
    AuthService authService;

    @Value("${jwt.secret.key}")
    private String secretKey;

    @Value("${jwt.issuer.name}")
    private String issuerName;

    @Test
    void buildJWT() {
        Date now = new Date();
        String token = authService.buildJWT(10001L);
        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer(issuerName)
                .build();
        DecodedJWT jwt = verifier.verify(token);
        Long userID = jwt.getClaim("userID").asLong();
        assertEquals(userID, 10001L);
        Date expiresAt = jwt.getExpiresAt();
        assertTrue(expiresAt.getTime() - now.getTime() - tokenExpireTime < 500);
    }
}