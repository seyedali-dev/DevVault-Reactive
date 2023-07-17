package com.dev.vault.helper.payload.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class RegisterRequest {
    @NotNull
    @NotBlank
    private String username;
    @NotNull
    @NotBlank
    @Pattern(regexp = "^" + //start of line
                      "(?=.*[0-9])" + //0-9 numbers
                      "(?=.*[a-z])" + //a-z letters
                      "(?=.*[A-Z])" + //A-Z letters
                      "(?=.*[!@#&()–\\[{}\\]:;',?/*~$^+=<>])" + // one of the special characters
                      "." + // matches anything
                      "{8,}" +// at least 8 characters
                      "$" // end of line
            , message = "Pass must be more than 8 characters & must contain a-z, A-Z, 0-9 and special characters.")
    private String password;
    @Email
    private String email;
    private int age;
    private String education;
    private String major;
}
