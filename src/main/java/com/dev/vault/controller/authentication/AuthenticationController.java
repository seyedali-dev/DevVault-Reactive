package com.dev.vault.controller.authentication;

import com.dev.vault.helper.payload.request.auth.AuthenticationRequest;
import com.dev.vault.helper.payload.request.auth.AuthenticationResponse;
import com.dev.vault.helper.payload.request.auth.RegisterRequest;
import com.dev.vault.helper.payload.response.ApiResponse;
import com.dev.vault.service.interfaces.user.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpStatus.CREATED;

/**
 * The AuthenticationController class is REST controller that handles the authentication and authorization of users.
 * It contains methods for registering a new user, verifying a user's account, and authenticating a user.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    /**
     * The register method registers a new user with the system.
     * It takes a RegisterRequest object as input and returns an AuthenticationResponse object.
     * The method is annotated with @PostMapping and @Valid to handle HTTP POST requests and validate the input.
     *
     * @param registerRequest The RegisterRequest object containing the user's information.
     * @return The AuthenticationResponse object containing the user's authentication token.
     */
    @PostMapping("/register")
    public Mono<ResponseEntity<AuthenticationResponse>> register(@RequestBody @Valid RegisterRequest registerRequest) {
        return authenticationService.registerUser(registerRequest)
                .map(authenticationResponse -> ResponseEntity.status(CREATED).body(authenticationResponse));
    }

    /**
     * The verifyAccount method verifies a user's account using a token.
     * It takes a token as input and returns an ApiResponse object.
     * The method is annotated with @RequestMapping to handle HTTP GET and POST requests.
     *
     * @param token The token used to verify the user's account.
     * @return The ApiResponse object containing the result of the verification.
     */
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = "/accountVerification/{token}")
    public Mono<ResponseEntity<ApiResponse>> verifyAccount(@PathVariable String token) {
        authenticationService.verifyAccount(token);
        return Mono.just(ResponseEntity.ok(new ApiResponse("account activated successfully", true)));
    }

    /**
     * The authenticate method authenticates a user with the system.
     * It takes an AuthenticationRequest object as input and returns an AuthenticationResponse object.
     * The method is annotated with @PostMapping to handle HTTP POST requests.
     *
     * @param authenticationRequest The AuthenticationRequest object containing the user's credentials.
     * @return The AuthenticationResponse object containing the user's authentication token.
     */
    @PostMapping({"/login", "/authenticate"})
    public Mono<ResponseEntity<AuthenticationResponse>> authenticate(@RequestBody AuthenticationRequest authenticationRequest) {
        return authenticationService.authenticate(authenticationRequest)
                .map(ResponseEntity::ok);
    }
}
