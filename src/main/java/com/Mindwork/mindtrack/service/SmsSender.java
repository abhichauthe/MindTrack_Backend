package com.Mindwork.mindtrack.service;

public interface SmsSender {
    void sendOtp(String phoneNumber, String otp);
}

