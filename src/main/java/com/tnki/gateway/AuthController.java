package com.tnki.gateway;

import com.tnki.gateway.request.LoginRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
public class AuthController {
    @Value("${jwt.header.key}")
    private String headerKey;

    WebClient client;
    AuthService authService;

    AuthController(@Value("${app.service.core}") String coreServiceUrl, AuthService authService) {
        this.client = WebClient.create(coreServiceUrl);
        this.authService = authService;
    }

    @PostMapping("/login")
    private Mono<ResponseEntity<Void>> login(@RequestBody LoginRequest loginRequest) {
        return client
                .post()
                .uri("authentication")
                .body(Mono.just(Map.of("username", loginRequest.getUsername(), "password", loginRequest.getPassword())), Map.class)
                .exchange()
                .flatMap(clientResponse -> {
                    if (!clientResponse.statusCode().equals(HttpStatus.OK)) {
                        return Mono.just(ResponseEntity.status(clientResponse.rawStatusCode()).build());
                    }
                    return clientResponse.bodyToMono(Long.class).flatMap(userID -> {
                        String jwt = authService.buildJWT(userID);
                        System.out.println(jwt);
                        return Mono.just(ResponseEntity.ok().header(headerKey, jwt).build());
                    });
                });
    }
}
