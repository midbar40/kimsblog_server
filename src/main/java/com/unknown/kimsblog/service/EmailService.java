package com.unknown.kimsblog.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@RequiredArgsConstructor
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    public void sendPasswordResetEmail(String toEmail, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("비밀번호 재설정 요청");

            String resetUrl = frontendUrl + "/reset-password?token=" + token;

            String htmlContent = buildPasswordResetEmailContent(resetUrl);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println("Password reset email sent to: " + toEmail);

        } catch (MessagingException e) {
            System.err.println("Failed to send password reset email: " + e.getMessage());
            throw new RuntimeException("이메일 전송에 실패했습니다.", e);
        }
    }

    private String buildPasswordResetEmailContent(String resetUrl) {
        return """
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2 style="color: #2563eb;">비밀번호 재설정</h2>
                    
                    <p>안녕하세요,</p>
                    
                    <p>비밀번호 재설정을 요청하셨습니다. 아래 링크를 클릭하여 새로운 비밀번호를 설정해주세요.</p>
                    
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="%s" 
                           style="background-color: #2563eb; color: white; padding: 12px 30px; 
                                  text-decoration: none; border-radius: 5px; display: inline-block;">
                            비밀번호 재설정
                        </a>
                    </div>
                    
                    <p style="color: #666; font-size: 14px;">
                        • 이 링크는 30분간 유효합니다.<br>
                        • 만약 비밀번호 재설정을 요청하지 않으셨다면, 이 이메일을 무시해주세요.
                    </p>
                    
                    <hr style="border: none; border-top: 1px solid #eee; margin: 30px 0;">
                    
                    <p style="color: #999; font-size: 12px;">
                        링크가 작동하지 않는 경우, 아래 URL을 복사하여 브라우저에 붙여넣어 주세요:<br>
                        %s
                    </p>
                </div>
            </body>
            </html>
            """.formatted(resetUrl, resetUrl);
    }
}