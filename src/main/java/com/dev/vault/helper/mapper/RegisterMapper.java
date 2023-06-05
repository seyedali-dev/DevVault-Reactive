package com.dev.vault.helper.mapper;

import com.dev.vault.helper.payload.auth.RegisterRequest;
import com.dev.vault.model.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RegisterMapper {
    private final PasswordEncoder passwordEncoder;

    public User toUser(RegisterRequest registerRequest) {
        User user = new User();

        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        user.setAge(registerRequest.getAge());
        user.setEducation(registerRequest.getEducation());
        user.setMajor(registerRequest.getMajor());
        user.setActive(false);

        return user;
    }
}
