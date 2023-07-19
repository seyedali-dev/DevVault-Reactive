package com.dev.vault.config;

import com.dev.vault.model.entity.user.User;
import com.dev.vault.util.repository.ReactiveRepositoryUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ApplicationConfiguration {

    private final ReactiveRepositoryUtils reactiveRepositoryUtils;

    /**
     * Returns a ReactiveUserDetailsService bean that retrieves user details from the database.
     *
     * @return a ReactiveUserDetailsService bean
     */
    /*@Bean
    public ReactiveUserDetailsService reactiveUserDetailsService() {
        return email -> reactiveRepositoryUtils.findUserByEmail_OrElseThrow_ResourceNotFoundException(email)
                .map(user -> new org.springframework.security.core.userdetails.User(
                                user.getUsername(),
                                user.getPassword(),
                                user.getUserRoles()
                                        .stream() //TODO:: remember this mistake and make a note somewhere [not including "ROLE_" leads to forbidden 403]
                                        .map(userRole -> {
                                            reactiveRepositoryUtils.findAllRoleByRoleId_OrElseThrow_ResourceNotFoundException(userRole.getRoleId())
                                                    .map(Roles::getRole)
                                                    .map(Enum::name)
                                                    .map(s -> new SimpleGrantedAuthority("ROLE_" + s));

                                        })
                                        .toList()
                        )
                );
    }*/
    @Bean
    public ReactiveUserDetailsService userDetailsService() {
        return email -> {
            Mono<User> userMono = reactiveRepositoryUtils.findUserByEmail_OrElseThrow_ResourceNotFoundException(email);

            return userMono.flatMap(user -> {
                /*List<GrantedAuthority> authorities = new ArrayList<>();
                // get the IDs of roles of the user
                List<String> listOfRoleIds = user.getUserRoles().stream().map(userRole -> userRole.getRoles().getRoleId()).toList();

                // then foreach roleID; map it to a Mono of `SimpleGrantedAuthority`
                listOfRoleIds.forEach(roleId ->
                        reactiveRepositoryUtils.findAllRoleByRoleId_OrElseThrow_ResourceNotFoundException(roleId)
                                .flatMap(roles -> Mono.just(new SimpleGrantedAuthority(roles.getRole().name())))
                                .doOnNext(simpleGrantedAuthority -> log.info("role that got added to SimpleGrantedAuthority: {}", simpleGrantedAuthority.getAuthority()))
                                // add the granted authority to the authority list
                                .doOnNext(authorities::add)
                                .doOnNext(simpleGrantedAuthority -> log.info("role that got added to authority list: {}", simpleGrantedAuthority.getAuthority()))
                );*/

                log.info("authorities of user: {}", user.getAuthorities().stream().toList());

                return Mono.just(new org.springframework.security.core.userdetails.User(
                        user.getEmail(),
                        user.getPassword(),
                        user.getAuthorities()
                ));
            });
        };
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
