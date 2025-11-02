package com.Globoo.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SmtpMailService implements MailService {

    private final JavaMailSender mailSender;

    @Value("${MAIL_FROM}")
    private String fromEmail;

    @Override
    public void sendVerificationMail(String toEmail, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("[Globoo] 학교 이메일 인증번호");

            String html = """
                    <div style="font-family:Segoe UI,Arial,sans-serif; line-height:1.6;">
                      <h2>Globoo 이메일 인증</h2>
                      <p>아래 인증번호를 Globoo 서비스에 입력해달라부! 감사합니다 ^o^</p>
                      <div style="margin:16px 0; padding:14px 18px; border:1px solid #eee; display:inline-block; font-size:24px; font-weight:700;">
                        %s
                      </div>
                      <p>유효시간: 24시간</p>
                      <p style="color:#888; font-size:12px;">본 메일은 인증을 위해 발송되었습니다.</p>
                    </div>
                    """.formatted(code);

            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("메일 발송 실패: " + e.getMessage(), e);
        }
    }
}
