package com.dev.vault.helper.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource, String name, String value) {
        super(String.format("%s not found with %s: %s", resource, name, value));
    }
}