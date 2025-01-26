package com.mataycode.auth;

public record AuthenticationRequest (
        String username,
        String password
) {
}
