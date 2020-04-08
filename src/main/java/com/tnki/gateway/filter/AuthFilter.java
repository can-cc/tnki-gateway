package com.tnki.gateway.filter;


import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@Component
public class AuthFilter implements GlobalFilter, Ordered {
    // private static final Logger LOGGER = LoggerFactory.getLogger(AuthFilter.class);

    @Value("${jwt.header.key}")
    private String headerKey;

    @Value("${jwt.secret.key}")
    private String secretKey;

    @Value("${jwt.issuer.name}")
    private String issuerName;

    @Override
    public int getOrder() {
        return -100;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String token = exchange.getRequest().getHeaders().getFirst(headerKey);
        if (token == null || token.isEmpty()) {
            return rejectUnAuthRequest(exchange);
        }
        try {
            Long userID = verifyClaimUserID(token);
            ServerHttpRequest mutableReq = exchange.getRequest().mutate().header("X-App-Auth-UserID", userID.toString()).build();
            ServerWebExchange mutableExchange = exchange.mutate().request(mutableReq).build();
            return chain.filter(mutableExchange);
        } catch (JWTVerificationException e) {
            return rejectUnAuthRequest(exchange);
        }
    }

    private Mono<Void> rejectUnAuthRequest(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.setComplete();
    }

    private Long verifyClaimUserID(String token) {
        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer(issuerName)
                .build();
        DecodedJWT jwt = verifier.verify(token);
        return jwt.getClaim("userID").asLong();
    }
}