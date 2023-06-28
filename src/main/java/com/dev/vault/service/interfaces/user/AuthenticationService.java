package com.dev.vault.service.interfaces.user;

import com.dev.vault.helper.payload.auth.AuthenticationRequest;
import com.dev.vault.helper.payload.auth.AuthenticationResponse;
import com.dev.vault.helper.payload.auth.RegisterRequest;
import com.dev.vault.model.user.User;

public interface AuthenticationService {
    AuthenticationResponse registerUser(RegisterRequest request);
    void verifyAccount(String token);
    AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest);
    User getCurrentUser();
}
