package com.dev.vault.helper.exception;

public class ResourceAlreadyExistsException extends RuntimeException {
    public ResourceAlreadyExistsException(String resource, String request, String value) {
        super(String.format("The Resource '%s', with '%s' value: '%s' already exists!", resource, request, value));
    }
}
