package com.Mindwork.mindtrack.controller;

import com.Mindwork.mindtrack.dto.AuthDto;
import com.Mindwork.mindtrack.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthDto.AuthResponse> register(
            @Valid @RequestBody AuthDto.RegisterRequest request
    ) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthDto.AuthResponse> login(
            @Valid @RequestBody AuthDto.LoginRequest request
    ) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/otp/request")
    public ResponseEntity<AuthDto.GenericMessageResponse> requestOtp(
            @Valid @RequestBody AuthDto.OtpRequest request
    ) {
        return ResponseEntity.ok(authService.requestOtp(request));
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<AuthDto.AuthResponse> verifyOtp(
            @Valid @RequestBody AuthDto.OtpVerifyRequest request
    ) {
        return ResponseEntity.ok(authService.verifyOtp(request));
    }

    @PostMapping("/password/reset-request")
    public ResponseEntity<AuthDto.GenericMessageResponse> resetRequest(
            @Valid @RequestBody AuthDto.PasswordResetRequest request,
            HttpServletRequest http
    ) {
        String ip = getClientIp(http);
        String baseUrl = "https://yourapp.com";
        return ResponseEntity.ok(authService.requestPasswordReset(request, ip, baseUrl));
    }

    @PostMapping("/password/reset-confirm")
    public ResponseEntity<AuthDto.GenericMessageResponse> resetConfirm(
            @Valid @RequestBody AuthDto.PasswordResetConfirmRequest request
    ) {
        return ResponseEntity.ok(authService.confirmPasswordReset(request));
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}