package com.dev.vault.config.security;

import com.dev.vault.config.jwt.JwtService;
import com.dev.vault.helper.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collection;

/**
 * A reactive implementation of the {@link ReactiveAuthenticationManager} interface that validates JWT tokens and returns an authenticated {@link Authentication} object.
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class RAuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtService jwtService;
    private final ReactiveUserDetailsService reactiveUserDetailsService;

    /**
     * Authenticates the JWT token in the input {@link Authentication} object and returns an authenticated {@link Authentication} object.
     *
     * @param authentication the input {@link Authentication} object containing the JWT token
     * @return a {@link Mono} object containing the authenticated {@link Authentication} object
     * @throws RuntimeException if the user is not found
     */
    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        // extract the username and the token
        String token = authentication.getCredentials().toString();
        String username = jwtService.extractUsername(token);

        // load the user from db
        return reactiveUserDetailsService.findByUsername(username)
                .map(userDetails -> {
                    Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
                    return new UsernamePasswordAuthenticationToken(
                            username,
                            token,
                            authorities
                    );
                }).map(usernamePasswordAuthenticationToken -> (Authentication) usernamePasswordAuthenticationToken)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("User", "Username", username)))
                .doOnError(throwable -> log.error("username not found ❌: {}", throwable.getMessage()));
    }

    /*
     * Detailed explanation:
     * The `AuthenticationManager` class is a reactive implementation of the `ReactiveAuthenticationManager` interface,
     * which is responsible for validating JWT tokens and returning an `Authentication` object if the token is valid.
     * The `authenticate` method takes an `Authentication` object as input and returns a `Mono<Authentication>` object as output.
     * The input `Authentication` object contains the JWT token to be validated, while the output `Mono<Authentication>` object contains the authenticated
     *  `Authentication` object.
     * The method first extracts the JWT token from the input `Authentication` object and extracts the username from the token using the `JWTUtil` object.
     * It then calls the `findByUsername` method of the `ReactiveUserDetailsService` object to load the user details for the given username. If the user is found, the method creates a `UsernamePasswordAuthenticationToken` object using the username, token, and granted authorities from the user details.
     * The method then returns the `UsernamePasswordAuthenticationToken` object wrapped in a `Mono` object.
     * If the user is not found, the method returns a `Mono` object with an error.
     */

}
