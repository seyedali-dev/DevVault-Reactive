package com.dev.vault.service.interfaces.user;

import com.dev.vault.helper.payload.request.auth.AuthenticationRequest;
import com.dev.vault.helper.payload.request.auth.AuthenticationResponse;
import com.dev.vault.helper.payload.request.auth.RegisterRequest;
import com.dev.vault.model.entity.user.User;
import reactor.core.publisher.Mono;

public interface AuthenticationService {
    Mono<AuthenticationResponse> registerUser(RegisterRequest request);
    void verifyAccount(String token);
    Mono<AuthenticationResponse> authenticate(AuthenticationRequest authenticationRequest);
    Mono<User> getCurrentUser();
}
