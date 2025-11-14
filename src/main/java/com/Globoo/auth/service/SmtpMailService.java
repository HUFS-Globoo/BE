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
                    <div style="
                            font-family: 'Segoe UI', Arial, sans-serif;
                            background: #f7fbff;
                            padding: 28px;
                            border-radius: 14px;
                            border: 1px solid #e3eefc;
                            max-width: 480px;
                            margin: auto;
                            box-shadow: 0 4px 10px rgba(180, 200, 230, 0.18);
                          ">
                    
                            <h2 style="
                              color: #3b82f6;
                              text-align: center;
                              margin-top: 0;
                              margin-bottom: 12px;
                              font-size: 22px;
                              font-weight: 700;
                            ">
                              Globoo 이메일 인증
                            </h2>
                    
                            <p style="
                              text-align: center;
                              color: #5b6b82;
                              font-size: 13px;
                              margin-bottom: 22px;
                            ">
                              아래 인증번호를 Globoo 서비스에 입력해달라부! \s
                              <br>오늘도 함께해줘서 고마워요 ^o^ 💙
                            </p>
                    
                            <div style="
                                background: #ffffff;
                                border: 2px dashed #b6d4ff;
                                padding: 18px 16px;
                                text-align: center;
                                border-radius: 12px;
                                font-size: 30px;
                                font-weight: 800;
                                color: #2563eb;
                                letter-spacing: 3px;
                                margin-bottom: 22px;
                                box-shadow: 0 2px 6px rgba(200, 215, 240, 0.25);
                            ">
                              %s
                            </div>
                    
                            <p style="
                              text-align: center;
                              font-size: 14px;
                              color: #6b7280;
                              margin-bottom: 6px;
                            ">
                              ⏰ 유효시간: <strong>24시간</strong>
                            </p>
                    
                            <p style="
                              text-align: center;
                              color: #a1a8b6;
                              font-size: 12px;
                              margin-top: 18px;
                            ">
                              본 메일은 인증을 위해 자동 발송되었습니다.
                            </p>
                          </div>
                    
                    """.formatted(code);

            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("메일 발송 실패: " + e.getMessage(), e);
        }
    }
}
