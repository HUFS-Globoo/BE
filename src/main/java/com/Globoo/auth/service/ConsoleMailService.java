// com/Globoo/auth/service/ConsoleMailService.java
// 로컬에서만!
package com.Globoo.auth.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("local")
public class ConsoleMailService implements MailService {
    @Override
    public void sendVerificationMail(String toEmail, String code) {
        System.out.println("[MAIL:LOCAL] to=" + toEmail + " code=" + code);
    }
}
