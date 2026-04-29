package com.Mindwork.mindtrack.service;

import com.Mindwork.mindtrack.dto.AuthDto;
import com.Mindwork.mindtrack.entity.User;
import com.Mindwork.mindtrack.exception.ConflictException;
import com.Mindwork.mindtrack.exception.NotFoundException;
import com.Mindwork.mindtrack.exception.UnauthorizedException;
import com.Mindwork.mindtrack.repository.PasswordResetTokenRepository;
import com.Mindwork.mindtrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.Mindwork.mindtrack.entity.PasswordResetToken;
import jakarta.transaction.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordResetEmailService passwordResetEmailService;
    private final OtpStore otpStore;
    private final RateLimiter rateLimiter;
    private final SmsSender smsSender;

    public AuthDto.AuthResponse register(AuthDto.RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("An account with this email already exists. Please login instead.");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("This username is already taken. Please choose another.");
        }
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new ConflictException("An account with this phone number already exists. Please login instead.");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(request.getPassword())
                .phoneNumber(request.getPhoneNumber())
                .build();

        User saved = userRepository.save(user);

        return new AuthDto.AuthResponse(
                saved.getId(),
                saved.getUsername(),
                saved.getEmail(),
                "Account created successfully"
        );
    }

    public AuthDto.AuthResponse login(AuthDto.LoginRequest request) {

        // ── Approach 2: Specific messages (pick this for MVP) ────────
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException(
                        "No account found with this email. Please register first."
                ));

        if (!user.getPassword().equals(request.getPassword())) {
            throw new UnauthorizedException(
                    "Incorrect password. Please try again."
            );
        }

        // ── Approach 1: Secure message (pick this for production) ────
        // User user = userRepository.findByEmail(request.getEmail())
        //         .orElseThrow(() -> new RuntimeException(
        //                 "No account found or incorrect password. Please check your details."
        //         ));
        // if (!user.getPassword().equals(request.getPassword())) {
        //     throw new RuntimeException(
        //             "No account found or incorrect password. Please check your details."
        //     );
        // }

        return new AuthDto.AuthResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                "Login successful"
        );
    }

    public AuthDto.GenericMessageResponse requestOtp(AuthDto.OtpRequest request) {
        // Rate limit: max 3 requests per phone per 10 minutes
        boolean allowed = rateLimiter.allow("otp:req:" + request.getPhoneNumber(), 3, Duration.ofMinutes(10).toMillis());
        if (!allowed) {
            // Still generic to avoid enumeration
            return new AuthDto.GenericMessageResponse("If the phone number is registered, an OTP has been sent.");
        }

        userRepository.findByPhoneNumber(request.getPhoneNumber()).ifPresent(user -> {
            String otp = String.format("%06d", (int) (Math.random() * 1_000_000));
            Instant expiresAt = Instant.now().plusSeconds(90);
            otpStore.put(request.getPhoneNumber(), otp, expiresAt);
            smsSender.sendOtp(request.getPhoneNumber(), otp);
        });

        // Always generic to avoid phone enumeration
        return new AuthDto.GenericMessageResponse("If the phone number is registered, an OTP has been sent.");
    }

    public AuthDto.AuthResponse verifyOtp(AuthDto.OtpVerifyRequest request) {
        OtpStore.OtpEntry entry = otpStore.get(request.getPhoneNumber())
                .orElseThrow(() -> new UnauthorizedException("OTP expired"));

        if (entry.attempts() >= 3) {
            throw new UnauthorizedException("Too many attempts. Please request a new OTP.");
        }

        if (!entry.otp().equals(request.getOtp())) {
            OtpStore.OtpEntry updated = otpStore.incrementAttempts(request.getPhoneNumber());
            if (updated.attempts() >= 3) {
                throw new UnauthorizedException("Too many attempts. Please request a new OTP.");
            }
            throw new UnauthorizedException("Invalid OTP");
        }

        otpStore.delete(request.getPhoneNumber());

        User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new UnauthorizedException("No account found for this phone number."));

        return new AuthDto.AuthResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                "OTP verified"
        );
    }

    public AuthDto.GenericMessageResponse requestPasswordReset(AuthDto.PasswordResetRequest request, String clientIp, String appBaseUrl) {
        // Rate limit: max 5 requests per IP per hour
        boolean allowed = rateLimiter.allow("pwdreset:req:" + clientIp, 5, Duration.ofHours(1).toMillis());
        if (allowed) {
            userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
                String token = UUID.randomUUID().toString();
                PasswordResetToken prt = PasswordResetToken.builder()
                        .token(token)
                        .user(user)
                        .expiresAt(Instant.now().plus(Duration.ofMinutes(20)))
                        .build();
                passwordResetTokenRepository.save(prt);

                String link = appBaseUrl + "/reset-password?token=" + token;
                passwordResetEmailService.sendPasswordResetLink(user.getEmail(), link);
            });
        }

        // Always generic to avoid email enumeration
        return new AuthDto.GenericMessageResponse("If an account exists for this email, a reset link has been sent.");
    }

    @Transactional
    public AuthDto.GenericMessageResponse confirmPasswordReset(AuthDto.PasswordResetConfirmRequest request) {
        PasswordResetToken prt = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new NotFoundException("Invalid reset token"));

        if (Instant.now().isAfter(prt.getExpiresAt())) {
            passwordResetTokenRepository.deleteByToken(request.getToken());
            throw new UnauthorizedException("Reset token expired");
        }

        User user = prt.getUser();
        user.setPassword(request.getNewPassword());
        userRepository.save(user);

        passwordResetTokenRepository.deleteByToken(request.getToken());
        return new AuthDto.GenericMessageResponse("Password reset successful");
    }
}