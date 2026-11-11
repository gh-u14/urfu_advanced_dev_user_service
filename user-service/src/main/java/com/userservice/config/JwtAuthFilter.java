package com.userservice.config;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.userservice.dto.ErrorResponse;
import com.userservice.util.JwtProvider;

import reactor.core.publisher.Mono;

@Component
@Order(-100)
public class JwtAuthFilter implements WebFilter {

    private final JwtProvider jwtProvider;
    private final ObjectMapper objectMapper;

    public JwtAuthFilter(JwtProvider jwtProvider, ObjectMapper objectMapper) {
        this.jwtProvider = jwtProvider;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        if (isPublic(request)) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange, "missing_token", "Authorization Bearer token is required");
        }

        String token = authHeader.substring(7).trim();
        if (token.isEmpty() || !jwtProvider.validate(token)) {
            return unauthorized(exchange, "invalid_token", "Token is invalid or expired");
        }

        Long userId = jwtProvider.getUserId(token);
        List<String> roles = jwtProvider.getRoles(token);
        exchange.getAttributes().put(AuthContext.ATTR_USER_ID, userId);
        exchange.getAttributes().put(AuthContext.ATTR_ROLES, roles);

        return chain.filter(exchange);
    }

    private static boolean isPublic(ServerHttpRequest request) {
        if (request.getMethod() == HttpMethod.OPTIONS) {
            return true;
        }

        String path = request.getPath().pathWithinApplication().value();
        HttpMethod method = request.getMethod();

        if (method == HttpMethod.POST && "/api/v1/auth/register".equals(path)) {
            return true;
        }
        if (method == HttpMethod.POST && "/api/v1/auth/login".equals(path)) {
            return true;
        }

        // Public for Messages Service inter-service calls
        if (method == HttpMethod.GET && "/api/v1/users".equals(path)) {
            return true;
        }
        if (method == HttpMethod.GET
                && path.startsWith("/api/v1/users/")
                && !path.equals("/api/v1/users/me")
                && !path.contains("/roles/")) {
            return true;
        }

        return false;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String error, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(new ErrorResponse(error, message));
        } catch (JsonProcessingException e) {
            bytes = ("{\"error\":\"" + error + "\",\"message\":\"" + message + "\"}")
                    .getBytes(StandardCharsets.UTF_8);
        }

        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}
