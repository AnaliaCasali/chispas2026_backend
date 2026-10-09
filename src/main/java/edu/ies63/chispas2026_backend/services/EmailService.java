package edu.ies63.chispas2026_backend.services;

public interface EmailService {
    void sendPasswordResetEmail(String toEmail, String resetToken);
}