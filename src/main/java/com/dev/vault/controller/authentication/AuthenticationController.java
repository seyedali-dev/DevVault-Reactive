package com.dev.vault.controller.authentication;

import com.dev.vault.helper.payload.ApiResponse;
import com.dev.vault.helper.payload.AuthenticationRequest;
import com.dev.vault.helper.payload.AuthenticationResponse;
import com.dev.vault.helper.payload.dto.RegisterRequest;
import com.dev.vault.model.user.User;
import com.dev.vault.service.module.user.AuthenticationService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {
    private AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody @Valid RegisterRequest registerRequest) {
        return new ResponseEntity<>(authenticationService.registerUser(registerRequest), HttpStatus.CREATED);
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = "/accountVerification/{token}")
    public ResponseEntity<ApiResponse> verifyAccount(@PathVariable String token) {
        authenticationService.verifyAccount(token);
        return new ResponseEntity<>(new ApiResponse("account activated successfully", true), HttpStatus.OK);
    }

    @PostMapping({"/login", "/authenticate"})
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest authenticationRequest) {
        return ResponseEntity.ok(authenticationService.authenticate(authenticationRequest));
    }
}
