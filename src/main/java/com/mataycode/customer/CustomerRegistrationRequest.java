package com.mataycode.customer;

public record CustomerRegistrationRequest(
        String name,
        String email,
        Integer age
) {
}