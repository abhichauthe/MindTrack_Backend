package com.Mindwork.mindtrack.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class PasswordResetEmailService {

    private final JavaMailSender mailSender;
    private final String fromEmail;

    public PasswordResetEmailService(
            JavaMailSender mailSender,
            @Value("${app.mail.from:${spring.mail.username:}}") String fromEmail
    ) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
    }

    public void sendPasswordResetLink(String toEmail, String link) {
        SimpleMailMessage msg = new SimpleMailMessage();
        if (fromEmail != null && !fromEmail.isBlank()) {
            msg.setFrom(fromEmail);
        }
        msg.setTo(toEmail);
        msg.setSubject("Reset your password");
        msg.setText("Click the link to reset your password:\n\n" + link + "\n\nIf you did not request this, you can ignore this email.");
        mailSender.send(msg);
    }
}

