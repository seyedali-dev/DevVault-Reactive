package com.dev.vault.config.handler;

import com.dev.vault.repository.user.jwt.JwtTokenReactiveRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.logout.ServerLogoutHandler;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogoutHandlerImpl implements ServerLogoutHandler {

    @Value("${token.prefix}")
    private String TOKEN_PREFIX;

    private final JwtTokenReactiveRepository jwtTokenReactiveRepository;


    /**
     * Logs out the user by invalidating their JWT token and deleting all their saved tokens.
     * This method is called when the user sends a logout request to the server.
     *
     * @param exchange       i don't know
     * @param authentication the authentication object for the user
     */
    @Override
    public Mono<Void> logout(WebFilterExchange exchange, Authentication authentication) {
        final String authHeader = exchange.getExchange().getRequest().getHeaders().getFirst("Authorization");

        // If the header is missing or does not start with the token prefix, log an error and return
        if (authHeader == null || !authHeader.startsWith(TOKEN_PREFIX)) {
            log.error("❌❌❌ Invalid token or header = null -> not authenticated to logout ❌❌❌");
            return Mono.empty();
        }

        final String token = authHeader.substring(TOKEN_PREFIX.length()); // which is 7
        return jwtTokenReactiveRepository.findByToken(token)
                .flatMap(jwtToken -> {
                    if (jwtToken == null)
                        return Mono.fromRunnable(() -> log.info("user logged out successfully ✅"));

                    // If the token is found, get the user ID and delete all their saved tokens from the database
                    String userId = jwtToken.getUser().getUserId();
                    jwtTokenReactiveRepository.findAllByUser_UserId(userId)
                            .forEach(savedToken ->
                                    jwtTokenReactiveRepository.deleteById(Long.valueOf(savedToken.getJwtTokenId()))
                            );
                    return Mono.fromRunnable(() -> log.warn("User {} has logged out ✅. Proceeding the delete his/her tokens ...", jwtToken.getUser().getUsername()));
                });
    }
}
