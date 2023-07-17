package com.dev.vault.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.logout.ServerLogoutHandler;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Configuration
@RequiredArgsConstructor
@EnableWebFluxSecurity
@SuppressWarnings("DefaultAnnotationParam")
@EnableReactiveMethodSecurity(useAuthorizationManager = true)
public class WebfluxSecurityConfig {

    private final ServerLogoutHandler logoutHandlerImpl;
    private final RAuthenticationManager authenticationManager;
    private final RSecurityContextRepository securityContextRepository;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity httpSecurity) {
        httpSecurity.csrf().disable()
                .exceptionHandling()
                .authenticationEntryPoint((serverWebExchange, exception) ->
                        Mono.fromRunnable(() -> serverWebExchange.getResponse().setStatusCode(UNAUTHORIZED))
                ).accessDeniedHandler((serverWebExchange, deniedException) ->
                        Mono.fromRunnable(() -> serverWebExchange.getResponse().setStatusCode(FORBIDDEN))
                ).and()
                .authenticationManager(authenticationManager)
                .securityContextRepository(securityContextRepository)
                .authorizeExchange(authorizeExchangeSpec ->
                        authorizeExchangeSpec
                                .pathMatchers("/api/v1/auth/**").permitAll()
                                .pathMatchers(HttpMethod.OPTIONS).permitAll()
                                .anyExchange().authenticated()
                ).logout().logoutUrl("/api/v1/auth/logout").logoutHandler(logoutHandlerImpl).logoutSuccessHandler(
                        (exchange, authentication) -> Mono.fromRunnable(ReactiveSecurityContextHolder::clearContext)
                )
        ;
        return httpSecurity.build();
    }
}
