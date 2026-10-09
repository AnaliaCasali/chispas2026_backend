package edu.ies63.chispas2026_backend.service;

public interface EmailService {
    void sendPasswordResetEmail(String toEmail, String resetToken);
}