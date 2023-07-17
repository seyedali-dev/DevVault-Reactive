package com.dev.vault.config;

import com.dev.vault.util.repository.ReactiveRepositoryUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfiguration {

    private final ReactiveRepositoryUtils reactiveRepositoryUtils;

    /**
     * Returns a ReactiveUserDetailsService bean that retrieves user details from the database.
     *
     * @return a ReactiveUserDetailsService bean
     */
    @Bean
    public ReactiveUserDetailsService reactiveUserDetailsService() {
        return email -> reactiveRepositoryUtils.findUserByEmail_OrElseThrow_ResourceNotFoundException(email)
                .map(user -> new org.springframework.security.core.userdetails.User(
                                user.getUsername(),
                                user.getPassword(),
                                user.getRoles()
                                        .stream() //TODO:: remember this mistake and make a note somewhere [not including "ROLE_" leads to forbidden 403]
                                        .map(roles -> new SimpleGrantedAuthority("ROLE_" + roles.getRole().name()))
                                        .toList()
                        )
                );
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> builder.simpleDateFormat("yyyy-MM-dd HH:mm");
    }
}
