package com.dev.vault.config.jwt.filter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        log.info("➡️ JwtAuthenticationEntryPoint commenced...");
        String errorMessage;
        switch (response.getStatus()) {
            case HttpServletResponse.SC_UNAUTHORIZED -> {
                errorMessage = "UnAuthorized: Server⚠️";
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, errorMessage);
            }
            case HttpServletResponse.SC_FORBIDDEN -> {
                errorMessage = "Forbidden: Server ⚠️❗";
                response.sendError(HttpServletResponse.SC_FORBIDDEN, errorMessage);
            }
            case HttpServletResponse.SC_NOT_FOUND -> {
                errorMessage = "Not Found: Server ⁉️";
                response.sendError(HttpServletResponse.SC_NOT_FOUND, errorMessage);
            }
            case HttpServletResponse.SC_INTERNAL_SERVER_ERROR -> {
                errorMessage = "Internal Server Error: Server 🪲";
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorMessage);
            }
            default -> {
                errorMessage = "Bad Request: Server 🤨😐😑😶🙄";
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, errorMessage);
            }
        }
        log.info(errorMessage);
        log.info("❌❌❌ JwtAuthenticationEntryPoint :: you are not authorized! ❌❌❌");
    }
}
