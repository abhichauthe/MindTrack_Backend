package com.Mindwork.mindtrack.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class AuthDto {

    @Data
    public static class RegisterRequest {
        @NotBlank(message = "username is required")
        @Size(min = 3, max = 50, message = "username must be 3-50 characters")
        private String username;

        @NotBlank(message = "email is required")
        @Email(message = "email must be valid")
        private String email;

        @NotBlank(message = "password is required")
        @Size(min = 6, max = 100, message = "password must be 6-100 characters")
        private String password;

        @NotBlank(message = "phoneNumber is required")
        @Pattern(
                regexp = "^\\+?[0-9]{10,15}$",
                message = "phoneNumber must be 10-15 digits, optionally prefixed with +"
        )
        @JsonAlias({"mobileNumber"})
        private String phoneNumber;
    }

    @Data
    public static class LoginRequest {
        @NotBlank(message = "email is required")
        @Email(message = "email must be valid")
        private String email;

        @NotBlank(message = "password is required")
        private String password;
    }

    @Data
    public static class AuthResponse {
        private Long userId;
        private String username;
        private String email;
        private String message;

        public AuthResponse(Long userId, String username, String email, String message) {
            this.userId = userId;
            this.username = username;
            this.email = email;
            this.message = message;
        }
    }

    @Data
    public static class GenericMessageResponse {
        private String message;

        public GenericMessageResponse(String message) {
            this.message = message;
        }
    }

    @Data
    public static class OtpRequest {
        @NotBlank(message = "phoneNumber is required")
        @Pattern(
                regexp = "^\\+?[0-9]{10,15}$",
                message = "phoneNumber must be 10-15 digits, optionally prefixed with +"
        )
        @JsonAlias({"mobileNumber", "phone"})
        private String phoneNumber;
    }

    @Data
    public static class OtpVerifyRequest {
        @NotBlank(message = "phoneNumber is required")
        @Pattern(
                regexp = "^\\+?[0-9]{10,15}$",
                message = "phoneNumber must be 10-15 digits, optionally prefixed with +"
        )
        @JsonAlias({"mobileNumber"})
        private String phoneNumber;

        @NotBlank(message = "otp is required")
        @Pattern(regexp = "^[0-9]{6}$", message = "otp must be 6 digits")
        private String otp;
    }

    @Data
    public static class PasswordResetRequest {
        @NotBlank(message = "email is required")
        @Email(message = "email must be valid")
        private String email;
    }

    @Data
    public static class PasswordResetConfirmRequest {
        @NotBlank(message = "token is required")
        private String token;

        @NotBlank(message = "newPassword is required")
        @Size(min = 6, max = 100, message = "newPassword must be 6-100 characters")
        private String newPassword;
    }
}