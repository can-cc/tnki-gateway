package com.tnki.gateway;

import com.tnki.gateway.request.LoginRequest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AuthControllerTest {

    public static MockWebServer mockBackEnd;

    @BeforeAll
    static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @Test
    void login(@Autowired AuthService authService) {
        AuthController authController = new AuthController(authService, String.format("http://localhost:%s", mockBackEnd.getPort()), "X-App-Auth-Token");
        mockBackEnd.enqueue(
                new MockResponse()
                        .setHeader("Content-Type", "application/json;charset=UTF-8")
                        .setResponseCode(200)
                        .setBody("10001")
        );

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("doge");
        loginRequest.setPassword("p2ssword");

        ResponseEntity responseEntity = authController.login(loginRequest).block();

        assert responseEntity != null;
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }
}