package com.dev.vault.service.module.user;

import com.dev.vault.config.jwt.JwtService;
import com.dev.vault.helper.exception.AuthenticationFailedException;
import com.dev.vault.helper.exception.ResourceAlreadyExistsException;
import com.dev.vault.helper.exception.ResourceNotFoundException;
import com.dev.vault.helper.payload.auth.AuthenticationRequest;
import com.dev.vault.helper.payload.auth.AuthenticationResponse;
import com.dev.vault.helper.payload.auth.RegisterRequest;
import com.dev.vault.helper.payload.email.Email;
import com.dev.vault.model.user.User;
import com.dev.vault.model.user.VerificationToken;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.dev.vault.model.user.enums.Roles.TEAM_MEMBER;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthenticationServiceImpl implements AuthenticationService {
    private static final String ACCOUNT_VERIFICATION_AUTH_URL = "http://localhost:8080/api/v1/auth/accountVerification/";
    private final VerificationTokenRepository verificationTokenRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final ModelMapper modelMapper;
    private final AuthenticationManager authenticationManager;

    // create a new user, and send verification account for activating him/her
    @Override
    public AuthenticationResponse registerUser(RegisterRequest registerRequest) {
        Optional<User> foundUser = userRepository.findByEmail(registerRequest.getEmail());

        if (foundUser.isPresent()) {
            log.info("❌ This user already exists! provide unique email. ❌");
            throw new ResourceAlreadyExistsException("User", "Email", registerRequest.getEmail());
        }
//        User user = registerMapper.toUser(registerRequest);
        User user = modelMapper.map(registerRequest, User.class);
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRoles(TEAM_MEMBER);
        user.setRolesDescription("➡️ Default Role for newly created users :)");

        userRepository.save(user);
        log.info("✅ User saved to db, attempting to send activation email...");
        String token = generateVerificationToken(user);
        mailService.sendEmail(new Email(
                "Please Activate Your Account",
                user.getEmail(),
                "Thank you for signing up to spring reddit app!" +
                "please click the url below to activate ur account: " + ACCOUNT_VERIFICATION_AUTH_URL + token));

        log.info("➡️ generating JWT token...");
        String jwtToken = jwtService.generateToken(user);

        return AuthenticationResponse.builder()
                .username(user.getUsername())
                .role(user.getRoles().name())
                .roleDescription(user.getRolesDescription())
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
        VerificationToken verificationToken =
                verificationTokenRepository.findByToken(token).orElseThrow(() -> new ResourceNotFoundException("Token", "token", token));
        verificationToken.getUser().setActive(true);
        log.info("✅✅✅ User is now Activated ✅✅✅");
    }

    // login and generate a JWT token
    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
        ));
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "Email", request.getEmail()));
        String token = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRoles().name())
                .roleDescription(user.getRolesDescription())
                .build();
    }

    // get the logged-in user
    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new AuthenticationFailedException("❌❌❌ User: '" + authentication.getName() + "' is not authorized! ❌❌❌"));
    }
}
