package com.Mindwork.mindtrack.service;

import java.time.Instant;
import java.util.Optional;

public interface OtpStore {

    OtpEntry put(String phoneNumber, String otp, Instant expiresAt);

    Optional<OtpEntry> get(String phoneNumber);

    void delete(String phoneNumber);

    OtpEntry incrementAttempts(String phoneNumber);

    record OtpEntry(String phoneNumber, String otp, Instant expiresAt, int attempts) { }
}

