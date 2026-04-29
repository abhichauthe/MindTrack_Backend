package com.Mindwork.mindtrack.service;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryOtpStore implements OtpStore {

    private record MutableOtp(String otp, Instant expiresAt, int attempts) { }

    private final ConcurrentHashMap<String, MutableOtp> store = new ConcurrentHashMap<>();

    @Override
    public OtpEntry put(String phoneNumber, String otp, Instant expiresAt) {
        store.put(phoneNumber, new MutableOtp(otp, expiresAt, 0));
        return new OtpEntry(phoneNumber, otp, expiresAt, 0);
    }

    @Override
    public Optional<OtpEntry> get(String phoneNumber) {
        MutableOtp v = store.get(phoneNumber);
        if (v == null) return Optional.empty();
        if (Instant.now().isAfter(v.expiresAt())) {
            store.remove(phoneNumber);
            return Optional.empty();
        }
        return Optional.of(new OtpEntry(phoneNumber, v.otp(), v.expiresAt(), v.attempts()));
    }

    @Override
    public void delete(String phoneNumber) {
        store.remove(phoneNumber);
    }

    @Override
    public OtpEntry incrementAttempts(String phoneNumber) {
        MutableOtp v = store.computeIfPresent(phoneNumber, (k, existing) ->
                new MutableOtp(existing.otp(), existing.expiresAt(), existing.attempts() + 1)
        );
        if (v == null) {
            throw new IllegalStateException("OTP not found");
        }
        return new OtpEntry(phoneNumber, v.otp(), v.expiresAt(), v.attempts());
    }
}

