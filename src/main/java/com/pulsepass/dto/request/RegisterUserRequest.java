package com.pulsepass.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record RegisterUserRequest(
        @NotBlank(message = "Username is required")
        @Size(max = 50, message = "Username must have at most 50 characters")
        String username,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 150, message = "Email must have at most 150 characters")
        String email,

        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name must have at most 100 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 100, message = "Last name must have at most 100 characters")
        String lastName,

        @Size(max = 30, message = "Phone must have at most 30 characters")
        String phone,

        @Size(max = 100, message = "City must have at most 100 characters")
        String city,

        @NotNull(message = "Birth date is required")
        LocalDate birthDate,

        // BCrypt solo usa los primeros 72 bytes; por eso el maximo
        @NotBlank(message = "Password is required")
        @Size(min = 12, max = 72, message = "Password must have between 12 and 72 characters")
        String password
) {


        @Override
        public String toString() {
                return "RegisterUserRequest[username=" + username + ", email=" + email + "]";
        }
}
