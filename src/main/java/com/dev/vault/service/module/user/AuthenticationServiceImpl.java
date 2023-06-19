package com.dev.vault.service.module.user;

import com.dev.vault.config.jwt.JwtService;
import com.dev.vault.helper.exception.AuthenticationFailedException;
import com.dev.vault.helper.exception.ResourceAlreadyExistsException;
import com.dev.vault.helper.exception.ResourceNotFoundException;
import com.dev.vault.helper.payload.auth.AuthenticationRequest;
import com.dev.vault.helper.payload.auth.AuthenticationResponse;
import com.dev.vault.helper.payload.auth.RegisterRequest;
import com.dev.vault.helper.payload.email.Email;
import com.dev.vault.model.user.Roles;
import com.dev.vault.model.user.User;
import com.dev.vault.model.user.VerificationToken;
import com.dev.vault.model.user.enums.Role;
import com.dev.vault.repository.user.RolesRepository;
import com.dev.vault.repository.user.UserRepository;
import com.dev.vault.repository.user.VerificationTokenRepository;
import com.dev.vault.service.AuthenticationService;
import com.dev.vault.service.module.mail.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthenticationServiceImpl implements AuthenticationService {
    private final RolesRepository rolesRepository;

    private static final String ACCOUNT_VERIFICATION_AUTH_URL = "http://localhost:8080/api/auth/accountVerification/";

    private final VerificationTokenRepository verificationTokenRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final ModelMapper modelMapper;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    // create a new user, and send verification account for activating him/her
    @Override
    public AuthenticationResponse registerUser(RegisterRequest registerRequest) {
        // check if user already exists in the database
        Optional<User> foundUser = userRepository.findByEmail(registerRequest.getEmail());

        if (foundUser.isPresent()) {
            log.info("❌ This user already exists! provide unique email. ❌");
            throw new ResourceAlreadyExistsException("User", "Email", registerRequest.getEmail());
        }
        // find the TEAM_MEMBER role and assign it to newly created user as default role
        Roles teamMemberRole = rolesRepository.findByRole(Role.TEAM_MEMBER)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "RoleName", Role.TEAM_MEMBER.name()));

        // create a new user object and map the properties from the register request
        User user = modelMapper.map(registerRequest, User.class);
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.getRoles().add(teamMemberRole);
        user.setRolesDescription("➡️➡️Default role for user is TEAM_MEMBER");

        // save the user object to the database
        userRepository.save(user);
        log.info("✅ User saved to db, attempting to send activation email...");

        // generate a verification token and send an email with the activation link
        String token = generateVerificationToken(user);
        mailService.sendEmail(new Email(
                "Please Activate Your Account",
                user.getEmail(),
                "Thank you for signing up to our app! " +
                "Please click the url below to activate your account: " + ACCOUNT_VERIFICATION_AUTH_URL + token));

        log.info("➡️ generating JWT token...");
        // generate and return a JWT token for the newly created user
        String jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .username(user.getUsername())
                .roles(user.getRoles()
                        .stream().map(roles -> roles.getRole().name())
                        .toList()
                )
                .rolesDescription(List.of("➡️➡️Default role for user is TEAM_MEMBER"))
                .token(jwtToken)
                .build();
    }

    // generate a verification token for the user that has attempted to sign-up
    private String generateVerificationToken(User user) {
        VerificationToken verificationToken = new VerificationToken(user);
        verificationTokenRepository.save(verificationToken);
        return verificationToken.getToken();
    }

    // verify the account and activate the user
    @Override
    public void verifyAccount(String token) {
        // find the verification token in the database
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Verification token", "token", token));

        // set the user's active status to true and save the changes to the database
        User user = verificationToken.getUser();
        user.setActive(true);
        userRepository.save(user);

        log.info("✅✅✅ User is now activated. ✅✅✅");
    }

    // login and generate a JWT token
    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        // authenticate the user's credentials using the authentication manager
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // get the user object from the authentication object and generate a JWT token
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "Email", userDetails.getUsername()));
        String jwtToken = jwtService.generateToken(user);

        // return the authentication response with the JWT token and user information
        return AuthenticationResponse.builder()
                .username(user.getUsername())
                .roles(user.getRoles()
                        .stream().map(roles -> roles.getRole().name())
                        .collect(Collectors.toList()))
                .token(jwtToken)
                .build();
    }

    // get the logged-in user
    @Override
    public User getCurrentUser() {
        // get the email of the currently authenticated user from the security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // find the user object in the database using the email
        Optional<User> foundUser = userRepository.findByEmail(email);

        return foundUser.orElseThrow(() -> new AuthenticationFailedException("❌❌❌ User: '" + email + "' is not authorized! ❌❌❌"));
    }
}
