package com.dev.vault.config.security;

import com.dev.vault.config.jwt.JwtService;
import com.dev.vault.helper.exception.DevVaultException;
import com.dev.vault.repository.user.jwt.JwtTokenReactiveRepository;
import com.dev.vault.util.repository.ReactiveRepositoryUtils;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

/**
 * An implementation of the {@link ServerSecurityContextRepository} interface that retrieves the {@link SecurityContext} object for the current request based on a JWT token in the request header.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RSecurityContextRepository implements ServerSecurityContextRepository {

    @Value("${token.prefix}")
    private String TOKEN_PREFIX;

    private final JwtTokenReactiveRepository jwtTokenReactiveRepository;
    private final ReactiveRepositoryUtils reactiveRepositoryUtils;
    private final RAuthenticationManager authenticationManager;
    private final JwtService jwtService;

    /**
     * Not implemented. Throws an {@link UnsupportedOperationException}.
     *
     * @param exchange the {@link ServerWebExchange} object
     * @param context  the {@link SecurityContext} object to save
     * @return a {@link Mono} object
     * @throws UnsupportedOperationException always
     */
    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        log.warn("Not supported yet.");
        return Mono.error(new UnsupportedOperationException("Not supported yet."));
    }


    /**
     * Retrieves the {@link SecurityContext} object for the current request based on a JWT token in the request header.
     *
     * @param exchange the {@link ServerWebExchange} object
     * @return a {@link Mono} object containing the {@link SecurityContext} object
     */
    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        log.info("extracting header ...");
        String authHeader = exchange.getRequest().getHeaders().getFirst(AUTHORIZATION);
        return Mono.justOrEmpty(authHeader)
//                .doOnNext(header -> log.info("extracted header 👍"))
                .filter(header -> header.startsWith(TOKEN_PREFIX))
//                .doOnNext(header -> log.info("Bearer token found in the authorization header 👍"))
                .flatMap(header -> {
                    String token = header.substring(TOKEN_PREFIX.length()); // which is seven
                    String userEmail;

                    try {
                        userEmail = jwtService.extractUsername(token);
                        log.info("username extracted :: {}", userEmail);
                    } catch (ExpiredJwtException e) {
                        log.error("JWT token has expired ❌");
                        e.printStackTrace();
                        return Mono.error(new DevVaultException("JWT token has expired ❌"));
                    } catch (IllegalArgumentException e) {
                        log.error("Invalid token request ❌");
                        e.printStackTrace();
                        return Mono.error(new DevVaultException("Invalid token request ❌"));
                    }

                    // find user and validate token
                    return reactiveRepositoryUtils.findUserByEmail_OrElseThrow_ResourceNotFoundException(userEmail)
                            .flatMap(user ->
                                    jwtTokenReactiveRepository.findByToken(token)
                                            .map(jwtToken -> !jwtToken.isRevoked() && !jwtToken.isExpired())
                                            .defaultIfEmpty(Boolean.FALSE)
                                            .flatMap(isTokenValid -> {
                                                        if (!isTokenValid && !jwtService.validateToken(token, user)) {
                                                            log.error("Invalid login Request! token not valid! ❌❌❌");
                                                            return Mono.error(new DevVaultException("Invalid login Request! user not valid! ❌❌❌"));
                                                        } else {
                                                            UsernamePasswordAuthenticationToken authentication =
                                                                    new UsernamePasswordAuthenticationToken(token, token);
                                                            return authenticationManager.authenticate(authentication)
                                                                    .doOnNext(auth -> {
                                                                        log.info("JWT token is valid! authentication successful ✅");
                                                                        log.info("------------------------------------------------");
                                                                    })
                                                                    .map(SecurityContextImpl::new);
                                                        }
                                                    }
                                            )
                            );
                });
    }

    /*@Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        // extract the header from request
        String header = exchange.getRequest().getHeaders().getFirst(AUTHORIZATION);
        return Mono.justOrEmpty(header)
                .flatMap(this::extractTokenFromHeader)
                .flatMap(this::authenticateToken)
                .switchIfEmpty(Mono.error(new AuthenticationCredentialsNotFoundException("Bearer token not found in the authorization header ❌")))
                .doOnNext(securityContext -> log.error("Bearer token not found in the authorization header ❌"));
    }*/

    /**
     * Extracts the bearer token from the authorization header.
     *
     * @param authHeader the authorization header
     * @return a Mono containing the extracted bearer token
     */
    /*private Mono<String> extractTokenFromHeader(String authHeader) {
        return Mono.justOrEmpty(authHeader)
                .doOnNext(header -> log.info("Authorization header extracted"))
                .filter(header -> header.startsWith(TOKEN_PREFIX))
                .map(header -> header.substring(TOKEN_PREFIX.length()))
                .doOnNext(token -> log.info("Bearer token found in the authorization header 👍: {}", token));
    }*/

    /**
     * Authenticates the given bearer token and returns a Mono containing the resulting SecurityContext.
     *
     * @param token the bearer token
     * @return a Mono containing the authenticated SecurityContext
     */
    /*private Mono<SecurityContext> authenticateToken(String token) {
        String userEmail;

        try {
            log.info("extracting username ...");
            userEmail = jwtService.extractUsername(token);
            log.info("Username extracted ::: {}", userEmail);
        } catch (ExpiredJwtException e) {
            log.error("JWT token has expired ❌");
            return Mono.error(new DevVaultException("JWT token has expired ❌"));
        } catch (IllegalArgumentException e) {
            log.error("Invalid token request ❌");
            return Mono.error(new DevVaultException("Invalid token request ❌"));
        }

        return reactiveRepositoryUtils.findUserByEmail_OrElseThrow_ResourceNotFoundException(userEmail)
                .flatMap(user ->
                        jwtTokenReactiveRepository.findByToken(token)
                                .map(jwtToken -> !jwtToken.isRevoked() && !jwtToken.isExpired())
                                .switchIfEmpty(Mono.error(new DevVaultException("👮🏻❌ JWT token is not valid! ❌👮🏻")))
                                .flatMap(isTokenValid -> {
                                    if (!isTokenValid && !jwtService.validateToken(token, user)) {
                                        log.error("Invalid login Request! token not valid! ❌❌❌");
                                        return Mono.error(new DevVaultException("Invalid login Request! user not valid! ❌❌❌"));
                                    } else {
                                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(token, token);

                                        // Authenticate the user's credentials using the reactive authentication manager
                                        return authenticationManager.authenticate(authentication)
                                                .doOnNext(auth -> {
                                                    log.info("JWT token is valid! ✅");
                                                    log.info("authentication successful ✅");
                                                })
                                                .map(SecurityContextImpl::new);
                                    }
                                })
                );
    }*/

    /*
     * Detailed explanation:
     * The SecurityContextRepository class is an implementation of the ServerSecurityContextRepository interface, which is responsible for
     * storing and retrieving the SecurityContext object for the current request.
     * The SecurityContextRepository constructor takes an AuthenticationManager object as argument.
     * The AuthenticationManager object is responsible for authenticating the user based on the JWT token.
     * The save method is not implemented and throws an UnsupportedOperationException. This is because //TODO: make an explanation on this
     * The load method retrieves the JWT token from the Authorization header in the HTTP request, extracts the token, and passes it to the
     * AuthenticationManager object for authentication.
     * If the token is valid, the AuthenticationManager returns an Authentication object, which is used to create a new SecurityContextImpl object
     * and returned wrapped in a Mono object.
     * If the token is not valid, the AuthenticationManager returns an error, and the load method returns an empty Mono object.
     */

}
