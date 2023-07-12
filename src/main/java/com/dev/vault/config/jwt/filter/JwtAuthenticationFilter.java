package com.dev.vault.config.jwt.filter;

import com.dev.vault.config.jwt.JwtService;
import com.dev.vault.helper.exception.DevVaultException;
import com.dev.vault.repository.user.jwt.JwtTokenRepository;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${token.prefix}")
    private String TOKEN_PREFIX;

    private final JwtTokenRepository jwtTokenRepository;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String token;
        final String userEmail;
        if (authHeader == null || !authHeader.startsWith(TOKEN_PREFIX)) {
            log.error("❌❌❌ Invalid token or header = null ❌❌❌");
            filterChain.doFilter(request, response);
            return;
        }
        log.info("header is valid ✅ ...");
        token = authHeader.substring(TOKEN_PREFIX.length()); // which is 7

        try {
            userEmail = jwtService.extractUsername(token);
            log.info("username extracted :: " + userEmail);
        } catch (ExpiredJwtException e) {
            log.error("JWT token has expired ❌");
            throw new ExpiredJwtException(null, null, "JWT token has been expired ❌", e);
        } catch (IllegalArgumentException e) {
            log.error("Invalid token request ❌");
            throw new DevVaultException("Invalid token request ❌");
        }

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
            Boolean isTokenValid = jwtTokenRepository.findByToken(token)
                    .map(jwtToken -> !jwtToken.isRevoked() && !jwtToken.isExpired())
                    .orElse(false);
            if (jwtService.validateToken(token, userDetails) && isTokenValid) {
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        } else {
            log.error("Invalid login Request! user not valid! ❌❌❌");
            throw new DevVaultException("Invalid login Request! user not valid! ❌❌❌");
        }
        log.info("JWT token is valid! ✅");
        filterChain.doFilter(request, response);
    }
}
