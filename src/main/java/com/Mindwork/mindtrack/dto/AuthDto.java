package com.Mindwork.mindtrack.dto;

import lombok.Data;

public class AuthDto {

    @Data
    public static class RegisterRequest {
        private String username;
        private String email;
        private String password;
    }

    @Data
    public static class LoginRequest {
        private String email;
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
}