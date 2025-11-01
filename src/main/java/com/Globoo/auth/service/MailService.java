package com.Globoo.auth.service;

public interface MailService {
    void sendVerificationMail(String toEmail, String token);
}
