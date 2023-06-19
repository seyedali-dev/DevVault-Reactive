package com.dev.vault.helper.exception;

public class NotLeaderOfProjectException extends RuntimeException {
    public NotLeaderOfProjectException(String s) {
        super(s);
    }
}
