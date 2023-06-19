package com.dev.vault.config.jwt.filter;

import com.dev.vault.config.jwt.JwtService;
import com.dev.vault.helper.exception.DevVaultException;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String token;
        final String userEmail;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.info("❌❌❌ JwtAuthenticationFilter :: Invalid token ❌❌❌");
            filterChain.doFilter(request, response);
            return;
        }
        log.info("header is valid ✅ ...");
        token = authHeader.substring(7);

        try {
            userEmail = jwtService.extractUsername(token);
            log.info("username extracted :: " + userEmail);
        } catch (ExpiredJwtException e) {
            log.info("JWT token has expired ❌");
            throw new ExpiredJwtException(null, null, "JWT token has been expired ❌", e);
        } catch (IllegalArgumentException e) {
            log.info("JwtAuthenticationFilter :: Invalid token request ❌");
            throw new DevVaultException("Invalid token request ❌");
        }

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
            if (jwtService.validateToken(token, userDetails)) {
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        } else {
            log.info("Invalid login Request! user not valid! ❌❌❌");
            throw new DevVaultException("Invalid login Request! user not valid! ❌❌❌");
        }
        log.info("JWT token is valid! ✅");
        filterChain.doFilter(request, response);
    }
}
