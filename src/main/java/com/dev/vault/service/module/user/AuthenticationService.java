package com.dev.vault.service.module.user;

import com.dev.vault.helper.payload.AuthenticationRequest;
import com.dev.vault.helper.payload.AuthenticationResponse;
import com.dev.vault.helper.payload.dto.RegisterRequest;

public interface AuthenticationService {
    AuthenticationResponse registerUser(RegisterRequest request);
    void verifyAccount(String token);
    AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest);
}
