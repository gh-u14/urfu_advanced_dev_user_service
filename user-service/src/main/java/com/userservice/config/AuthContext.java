package com.userservice.config;

import java.util.List;

public final class AuthContext {

    public static final String ATTR_USER_ID = "auth.userId";
    public static final String ATTR_ROLES = "auth.roles";

    private AuthContext() {
    }

    public static Long requireUserId(org.springframework.web.server.ServerWebExchange exchange) {
        Object value = exchange.getAttribute(ATTR_USER_ID);
        if (value instanceof Long userId) {
            return userId;
        }
        throw new IllegalStateException("Authenticated user id is missing");
    }

    @SuppressWarnings("unchecked")
    public static List<String> roles(org.springframework.web.server.ServerWebExchange exchange) {
        Object value = exchange.getAttribute(ATTR_ROLES);
        if (value instanceof List<?> list) {
            return (List<String>) list;
        }
        return List.of();
    }
}
