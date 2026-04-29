package com.Mindwork.mindtrack.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NoopSmsSender implements SmsSender {

    private static final Logger log = LoggerFactory.getLogger(NoopSmsSender.class);

    @Override
    public void sendOtp(String phoneNumber, String otp) {
        // Placeholder until you wire Twilio/AWS SNS/etc.
        log.info("OTP for {} is {}", phoneNumber, otp);
    }
}

