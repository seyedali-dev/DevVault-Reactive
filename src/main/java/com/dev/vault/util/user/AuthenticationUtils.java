package com.dev.vault.util.user;

import com.dev.vault.config.jwt.JwtService;
import com.dev.vault.helper.payload.request.auth.AuthenticationResponse;
import com.dev.vault.helper.payload.request.auth.RegisterRequest;
import com.dev.vault.helper.payload.request.email.Email;
import com.dev.vault.model.entity.user.Roles;
import com.dev.vault.model.entity.user.User;
import com.dev.vault.model.entity.user.UserRole;
import com.dev.vault.model.entity.user.VerificationToken;
import com.dev.vault.model.entity.user.jwt.JwtToken;
import com.dev.vault.model.enums.TokenType;
import com.dev.vault.repository.user.RolesReactiveRepository;
import com.dev.vault.repository.user.UserReactiveRepository;
import com.dev.vault.repository.user.UserRoleReactiveRepository;
import com.dev.vault.repository.user.VerificationTokenReactiveRepository;
import com.dev.vault.repository.user.jwt.JwtTokenReactiveRepository;
import com.dev.vault.service.module.mail.MailService;
import com.dev.vault.util.repository.ReactiveRepositoryUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static com.dev.vault.model.enums.Role.TEAM_MEMBER;

/**
 * A utility class that provides helper methods for authentication and registration of user.
 * This class contains method for revoking the JWT token, build and saving JWT token, generating verification token.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationUtils {
    private final UserRoleReactiveRepository userRoleReactiveRepository;

    @Value("${account.verification.auth.url}")
    private String ACCOUNT_VERIFICATION_AUTH_URL;

    private final VerificationTokenReactiveRepository verificationTokenReactiveRepository;
    private final JwtTokenReactiveRepository jwtTokenReactiveRepository;
    private final RolesReactiveRepository rolesReactiveRepository;
    private final UserReactiveRepository userReactiveRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final ModelMapper modelMapper;
    private final ReactiveRepositoryUtils reactiveRepositoryUtils;


    public Mono<AuthenticationResponse> createNewUser(RegisterRequest registerRequest) {
        return reactiveRepositoryUtils.findRoleByRole_OrElseThrow_ResourceNotFoundException(TEAM_MEMBER)
                .flatMap(teamMemberRole -> {
                    User user = createUserFromRequest(registerRequest);
                    log.info("userID: {}", user.getUserId());
                    Mono<User> savedUserMono = userReactiveRepository.save(user);
                    log.info("✅ User saved to db, attempting to send activation email...");

                    return Mono.zip(Mono.just(teamMemberRole), savedUserMono)
                            .flatMap(tuple -> {
                                Roles roles = tuple.getT1();
                                User savedUser = tuple.getT2();
                                return sendVerificationEmail(savedUser)
                                        .flatMap(jwtToken -> buildAuthenticationResponse(savedUser, jwtToken));
                            });
                });
    }

    private User createUserFromRequest(RegisterRequest registerRequest) {
        User user = modelMapper.map(registerRequest, User.class);
        user.setUserId(UUID.randomUUID().toString());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setActive(false);

        rolesReactiveRepository.findByRole(TEAM_MEMBER)
                .flatMap(roles -> {
                    // create a new user_roles document and save it
                    UserRole userRole = UserRole.builder()
                            .user(user)
                            .roles(roles)
                            .build();
                    userRoleReactiveRepository.save(userRole)
                            .doOnNext(ur -> log.info("userRole saved: {}", ur))
                            .doOnNext(ur -> log.info("userRole, roles saved: {}", ur.getRoles().getRole().name()))
                            .subscribe();
                    return Mono.just(userRole);
                }).subscribe();

        return user;
    }

    private Mono<String> sendVerificationEmail(User user) {
        String verificationToken = generateVerificationToken(user);
        mailService.sendEmail(new Email(
                "Please Activate Your Account",
                user.getEmail(),
                "Thank you for signing up to our app! " +
                "Please click the url below to activate your account: " + ACCOUNT_VERIFICATION_AUTH_URL + verificationToken)
        );

        log.info("➡️ generating JWT token...");
        return Mono.just(jwtService.generateToken(user))
                .flatMap(jwtToken -> buildAndSaveJwtToken(user, jwtToken)
                        .flatMap(token -> Mono.just(token.getToken()))
                );
    }

    private Mono<AuthenticationResponse> buildAuthenticationResponse(User user, String jwtToken) {
        return reactiveRepositoryUtils.findAllUserRolesByUserId_OrElseThrow_ResourceNotFoundException(user.getUserId())
                .map(userRole -> userRole.getRoles().getRole().name())
                .collectList()
                .flatMap(rolesList ->
                        Mono.just(AuthenticationResponse.builder()
                                .username(user.getUsername())
                                .roles(rolesList)
                                .rolesDescription(List.of("➡️➡️Default role for user is TEAM_MEMBER"))
                                .token(jwtToken)
                                .build()
                        ).doOnSuccess(authenticationResponse -> log.info("roles that got build-> {}", authenticationResponse.getRoles()))
                );
    }


    /**
     * Builds and saves a JWT token for the specified user.
     *
     * @param user  the {@link User User} object for which to generate the JWT token
     * @param token the JWT token to save
     */
    public Mono<JwtToken> buildAndSaveJwtToken(User user, String token) {
        // Build a new JwtToken object with the specified user and token, and save it to the database
        JwtToken jwtToken = JwtToken.builder()
                .expired(false)
                .revoked(false)
                .type(TokenType.BEARER)
                .user(user)
                .token(token)
                .build();
        return jwtTokenReactiveRepository.save(jwtToken);
    }


    /**
     * Revokes all valid tokens for the specified user by setting their 'expired' and 'revoked' flags to true.
     *
     * @param user the {@link User User} object for which to revoke all tokens
     */
    public void revokeAllUserTokens(User user) {
        // Find all valid tokens for the specified user
        jwtTokenReactiveRepository.findAllByUser_UserIdAndExpiredIsFalseAndRevokedIsFalse(user.getUserId())
                // If no valid tokens were found, return without modifying the database
                .switchIfEmpty(emptyToken -> {
                }).map(jwtTokens -> {

                    // Iterate through all valid tokens and set their 'expired' and 'revoked' flags to true
                    jwtTokens.forEach(token -> {
                        token.setExpired(true);
                        token.setRevoked(true);
                    });

                    return jwtTokenReactiveRepository.saveAll(jwtTokens);
                })
        ;
    }


    /**
     * Generates a verification token for the user that has attempted to sign-up.
     *
     * @param user the user to generate the verification token for
     * @return the verification token as a string
     */
    public String generateVerificationToken(User user) {
        VerificationToken verificationToken = new VerificationToken(user);
        verificationTokenReactiveRepository.save(verificationToken);
        return verificationToken.getToken();
    }
}
