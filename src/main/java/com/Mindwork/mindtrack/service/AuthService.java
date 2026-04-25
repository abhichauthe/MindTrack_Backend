package com.Mindwork.mindtrack.service;

import com.Mindwork.mindtrack.dto.AuthDto;
import com.Mindwork.mindtrack.entity.User;
import com.Mindwork.mindtrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    public AuthDto.AuthResponse register(AuthDto.RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("An account with this email already exists. Please login instead.");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("This username is already taken. Please choose another.");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(request.getPassword())
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
                .orElseThrow(() -> new RuntimeException(
                        "No account found with this email. Please register first."
                ));

        if (!user.getPassword().equals(request.getPassword())) {
            throw new RuntimeException(
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
}