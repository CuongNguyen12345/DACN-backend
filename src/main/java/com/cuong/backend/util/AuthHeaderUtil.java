package com.cuong.backend.util;

public final class AuthHeaderUtil {
    private static final String BEARER_PREFIX = "Bearer ";

    private AuthHeaderUtil() {
    }

    public static String stripBearerPrefix(String token) {
        if (token != null && token.startsWith(BEARER_PREFIX)) {
            return token.substring(BEARER_PREFIX.length());
        }
        return token;
    }
}
