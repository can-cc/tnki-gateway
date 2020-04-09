package com.tnki.gateway.filter;

import com.tnki.gateway.AuthService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AuthFilterTest {

    @Autowired
    AuthFilter authFilter;

    @Autowired
    AuthService authService;

    @Test
    void filter_unauthorized() {
        MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost:3500").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        GatewayFilterChain chain = Mockito.mock(GatewayFilterChain.class);
        authFilter.filter(exchange, chain);
        HttpStatus code = exchange.getResponse().getStatusCode();
        assertEquals(HttpStatus.UNAUTHORIZED, code);
    }

    @Test
    void filter_authorized() {
        String token = authService.buildJWT(10001L);
        MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost:3500").header("X-App-Auth-Token", token).build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        ArgumentCaptor<ServerWebExchange> argumentCaptor = ArgumentCaptor.forClass(ServerWebExchange.class);
        GatewayFilterChain chain = Mockito.mock(GatewayFilterChain.class);
        authFilter.filter(exchange, chain);
        Mockito.verify(chain).filter(argumentCaptor.capture());
        assertEquals("10001", argumentCaptor.getValue().getRequest().getHeaders().getFirst("X-App-Auth-UserID"));
    }
}