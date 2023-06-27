package com.dev.vault.helper.exception;

import io.jsonwebtoken.ExpiredJwtException;

public class JwtExpiredException extends RuntimeException {
    public JwtExpiredException(String s, ExpiredJwtException e) {
        super(s);
    }
}
