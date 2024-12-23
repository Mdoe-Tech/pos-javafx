package com.nadia.pos.exceptions;

import java.io.Serial;

public class AuthenticationException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}