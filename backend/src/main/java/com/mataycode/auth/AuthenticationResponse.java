package com.mataycode.auth;

import com.mataycode.customer.CustomerDTO;

public record AuthenticationResponse (
        String token,
        CustomerDTO customerDTO
        ) {
}
