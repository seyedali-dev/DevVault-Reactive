package com.dev.vault.service.module.user;

import com.dev.vault.config.jwt.JwtService;
import com.dev.vault.helper.exception.ResourceAlreadyExistsException;
import com.dev.vault.helper.exception.ResourceNotFoundException;
import com.dev.vault.helper.payload.request.auth.AuthenticationRequest;
import com.dev.vault.helper.payload.request.auth.AuthenticationResponse;
import com.dev.vault.helper.payload.request.auth.RegisterRequest;
import com.dev.vault.model.entity.user.User;
import com.dev.vault.model.entity.user.VerificationToken;
import com.dev.vault.repository.user.UserReactiveRepository;
import com.dev.vault.repository.user.VerificationTokenReactiveRepository;
import com.dev.vault.service.interfaces.user.AuthenticationService;
import com.dev.vault.util.repository.ReactiveRepositoryUtils;
import com.dev.vault.util.user.AuthenticationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

/**
 * Authentication implementation: Registration & Login.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthenticationServiceImpl implements AuthenticationService {
    private final VerificationTokenReactiveRepository verificationTokenReactiveRepository;

    private final UserReactiveRepository userReactiveRepository;
    private final AuthenticationUtils authenticationUtils;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ReactiveRepositoryUtils reactiveRepositoryUtils;


    /**
     * Registers a new user, assigns the <code>TEAM_MEMBER</code> role, saves the user to the database,
     * generates a verification token, sends an activation email, generates a JWT token,
     * saves the JWT token, and returns an {@link AuthenticationResponse AuthenticationResponse} object with the JWT token and user information.
     *
     * @param registerRequest the {@link RegisterRequest RegisterRequest} object containing the user's registration information
     * @return an {@link AuthenticationResponse AuthenticationResponse} object containing the JWT token and user information
     * @throws ResourceAlreadyExistsException if the user already exists in the database
     */
    //my implementation
    /*@Override
    public Mono<AuthenticationResponse> registerUser(RegisterRequest registerRequest) {
        // check if user already exists in the database
        return userReactiveRepository.findByEmail(registerRequest.getEmail())
                .hasElement()
                .flatMap(foundUser -> {
                    if (foundUser) {
                        log.info("❌ This user already exists! provide unique email. ❌");
                        return Mono.error(new ResourceAlreadyExistsException("User", "Email", registerRequest.getEmail()));
                    } else {
                        // find the TEAM_MEMBER role and assign it to newly created user as default role
                        Mono<Roles> teamMemberRole = repositoryUtils.findRoleByRole_OrElseThrow_ResourceNotFoundException(TEAM_MEMBER)
                                .switchIfEmpty(Mono.just(new Roles()))
                                .flatMap(rolesReactiveRepository::save);

                        // create a new user object and map the properties from the register request
                        User user = modelMapper.map(registerRequest, User.class);
                        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
                        user.setActive(false);

                        // save the user object to the database
                        Mono<User> savedUserMono = userReactiveRepository.save(user);
                        log.info("✅ User saved to db, attempting to send activation email...");

                        return Mono.zip(teamMemberRole, savedUserMono)
                                .flatMap(tuple -> {
                                    Roles roles = tuple.getT1();
                                    User savedUser = tuple.getT2();
                                    savedUser.getRoles().add(roles);

                                    // generate a verification token and send an email with the activation link
                                    String verificationToken = authenticationUtils.generateVerificationToken(user);
                                    mailService.sendEmail(new Email(
                                            "Please Activate Your Account",
                                            user.getEmail(),
                                            "Thank you for signing up to our app! " +
                                            "Please click the url below to activate your account: " + ACCOUNT_VERIFICATION_AUTH_URL + verificationToken));

                                    log.info("➡️ generating JWT token...");
                                    // generate and return a JWT token for the newly created user
                                    String jwtToken = jwtService.generateToken(user);

                                    // save the generated token
                                    return authenticationUtils.buildAndSaveJwtToken(savedUser, jwtToken)
                                            .flatMap(savedJwtToken ->
                                                    Mono.just(
                                                            AuthenticationResponse.builder()
                                                                    .username(savedUser.getUsername())
                                                                    .roles(savedUser.getRoles()
                                                                            .stream().map(Roles::getRole)
                                                                            .map(Enum::name)
                                                                            .toList()
                                                                    )
                                                                    .rolesDescription(List.of("➡️➡️Default role for user is TEAM_MEMBER"))
                                                                    .token(jwtToken)
                                                                    .build()
                                                    )
                                            );
                                });
                    }
                });
    }*/

    //chatgpt implementation with the use of transform
    /*public Mono<AuthenticationResponse> registerUser(RegisterRequest registerRequest) {
    Mono<Boolean> userExistsMono = userReactiveRepository.findByEmail(registerRequest.getEmail())
            .hasElement();

    Mono<AuthenticationResponse> responseMono = Mono.just(registerRequest)
            .transform(this::createNewUser)
            .onErrorResume(ResourceAlreadyExistsException.class,
                    error -> Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, error.getMessage())));

    return Mono.zip(userExistsMono, responseMono)
            .flatMap(tuple -> {
                Boolean userExists = tuple.getT1();
                AuthenticationResponse response = tuple.getT2();

                if (userExists) {
                    return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, "User already exists"));
                } else {
                    return Mono.just(response);
                }
            });
}

private Mono<AuthenticationResponse> createNewUser(Mono<RegisterRequest> requestMono) {
    return requestMono.flatMap(registerRequest -> {
        return repositoryUtils.findRoleByRole_OrElseThrow_ResourceNotFoundException(TEAM_MEMBER)
                .switchIfEmpty(Mono.just(new Roles()))
                .flatMap(rolesReactiveRepository::save)
                .flatMap(teamMemberRole -> {
                    User user = createUserFromRequest(registerRequest);
                    Mono<User> savedUserMono = saveUser(user);

                    return Mono.zip(Mono.just(teamMemberRole), savedUserMono)
                            .flatMap(tuple -> {
                                Roles roles = tuple.getT1();
                                User savedUser = tuple.getT2();
                                savedUser.getRoles().add(roles);

                                return sendVerificationEmail(savedUser, registerRequest)
                                        .flatMap(jwtToken -> buildAuthenticationResponse(savedUser, jwtToken));
                            });
                });
    });
}*/
    @Override
    public Mono<AuthenticationResponse> registerUser(RegisterRequest registerRequest) {
        return userReactiveRepository.findByEmail(registerRequest.getEmail())
                .hasElement()
                .flatMap(userExist -> {
                            if (userExist) {
                                log.info("❌ This user already exists! provide unique email. ❌");
                                return Mono.error(new ResourceAlreadyExistsException("User", "Email", registerRequest.getEmail()));
                            } else
                                return authenticationUtils.createNewUser(registerRequest);
                        }
                );
    }


    /**
     * Verifies the user's account and activates it.
     *
     * @param token the verification token
     */
    @Override
    public void verifyAccount(String token) {
        // find the verification token in the database
        Mono<VerificationToken> verificationTokenMono = verificationTokenReactiveRepository.findByToken(token)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Verification token", "token", token)));

        // set the user's active status to true and save the changes to the database
        verificationTokenMono.flatMap(verificationToken -> {
            User user = verificationToken.getUser();
            user.setActive(true);

            return userReactiveRepository.save(user);
        }).then(Mono.fromRunnable(() ->
                log.info("✅✅✅ User is now activated. ✅✅✅"))
        );
    }


    /**
     * Authenticates the user's credentials and generates a JWT token.
     * Revokes all existing tokens for the user and saves the new token.
     *
     * @param authenticationRequest the authentication request containing the user's email and password
     * @return an AuthenticationResponse object containing the JWT token and user information
     */
    /*@Override
    public Mono<AuthenticationResponse> authenticate(AuthenticationRequest authenticationRequest) {
        return reactiveRepositoryUtils.findUserByEmail_OrElseThrow_ResourceNotFoundException(authenticationRequest.getEmail())
                .filter(userDetails ->
                        passwordEncoder.matches(
                                authenticationRequest.getPassword(),
                                userDetails.getPassword()
                        )
                )
                // Get the user object from the authentication object and generate a JWT token
                .map(userDetails -> {
                    String generatedJwtToken = jwtService.generateToken(userDetails);

                    // Revoke all the saved tokens for the user and save the generated token
                    authenticationUtils.revokeAllUserTokens(userDetails);
                    authenticationUtils.buildAndSaveJwtToken(userDetails, generatedJwtToken);

                    // Return the authentication response with the JWT token and user information
                    return AuthenticationResponse.builder()
                            .username(userDetails.getUsername())
                            .roles(userDetails.getRoles()
                                    .stream().map(roles -> roles.getRole().name())
                                    .collect(Collectors.toList()))
                            .token(generatedJwtToken)
                            .build();
                }).switchIfEmpty(Mono.error(new AuthenticationCredentialsNotFoundException("You are not authenticated!")));
    }*/
    @Override
    public Mono<AuthenticationResponse> authenticate(AuthenticationRequest authenticationRequest) {
        return reactiveRepositoryUtils.findUserByEmail_OrElseThrow_ResourceNotFoundException(authenticationRequest.getEmail())
                .flatMap(user -> {
                    boolean passwordMatches = passwordEncoder.matches(authenticationRequest.getPassword(), user.getPassword());
                    if (passwordMatches) {
                        String generatedJwtToken = jwtService.generateToken(user);

                        // Revoke all the saved tokens for the user and save the generated token
                        authenticationUtils.revokeAllUserTokens(user);
                        return authenticationUtils.buildAndSaveJwtToken(user, generatedJwtToken)
                                .flatMap(jwtToken -> reactiveRepositoryUtils.findAllUserRolesByUserId_OrElseThrow_ResourceNotFoundException(user.getUserId())
                                        .map(userRole -> userRole.getRoles().getRole().name())
                                        .collectList()
                                        .map(roleNames -> {
                                                    log.info("roles list: {}", roleNames);
                                                    return AuthenticationResponse.builder()
                                                            .username(user.getUsername())
                                                            .roles(roleNames)
                                                            .token(jwtToken.getToken())
                                                            .build();
                                                }
                                        )
                                );
                    } else
                        return Mono.error(new BadCredentialsException("Invalid username or password"));
                }).switchIfEmpty(Mono.error(new BadCredentialsException("Invalid username or password")));
    }


    /**
     * Retrieves the currently logged-in user.
     *
     * @return the logged-in user
     */
    @Override
    public Mono<User> getCurrentUserMono() {
        // get the email of the currently authenticated user from the reactive security context
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .flatMap(authentication -> {
                            String email = authentication.getName();

                            // find the user object in the database using the email
                            return reactiveRepositoryUtils.findUserByEmail_OrElseThrow_ResourceNotFoundException(email);
                        }
                );
    }

}
