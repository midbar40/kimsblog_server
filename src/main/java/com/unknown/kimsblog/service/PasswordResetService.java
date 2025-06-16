package com.unknown.kimsblog.service;

import com.unknown.kimsblog.model.PasswordResetToken;
import com.unknown.kimsblog.model.User;
import com.unknown.kimsblog.repository.PasswordResetTokenRepository;
import com.unknown.kimsblog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public void sendPasswordResetEmail(String email) {
        System.out.println("=== Password Reset Request ===");
        System.out.println("Email: " + email);

        // 사용자 존재 확인
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            System.out.println("User not found for email: " + email);
            // 보안상 사용자가 존재하지 않아도 성공한 것처럼 응답
            return;
        }

        // 기존 토큰들 무효화
        tokenRepository.markAllTokensAsUsedForEmail(email);

        // 새 토큰 생성 (30분 유효)
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .email(email)
                .expiresAt(expiresAt)
                .build();

        tokenRepository.save(resetToken);

        // 이메일 전송
        try {
            emailService.sendPasswordResetEmail(email, token);
            System.out.println("Password reset email sent successfully to: " + email);
        } catch (Exception e) {
            System.err.println("Failed to send password reset email: " + e.getMessage());
            throw new RuntimeException("이메일 전송에 실패했습니다.");
        }
    }

    public boolean validateResetToken(String token) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);
        return tokenOpt.isPresent() && tokenOpt.get().isValid();
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        System.out.println("=== Password Reset ===");
        System.out.println("Token: " + token);

        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);

        if (tokenOpt.isEmpty()) {
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
        }

        PasswordResetToken resetToken = tokenOpt.get();

        if (!resetToken.isValid()) {
            throw new IllegalArgumentException("만료되었거나 이미 사용된 토큰입니다.");
        }

        // 사용자 찾기
        Optional<User> userOpt = userRepository.findByEmail(resetToken.getEmail());
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }

        User user = userOpt.get();

        // 비밀번호 업데이트 (User 엔티티에 setter 메서드 추가 필요)
        updateUserPassword(user, newPassword);

        // 토큰을 사용됨으로 표시
        resetToken.markAsUsed();
        tokenRepository.save(resetToken);

        System.out.println("Password reset successful for: " + user.getEmail());
    }

    private void updateUserPassword(User user, String newPassword) {
        // User 엔티티에 비밀번호 업데이트 메서드가 필요합니다.
        // 여기서는 리플렉션을 사용하거나, User 엔티티에 setter를 추가해야 합니다.

        // 임시로 직접 업데이트 쿼리 사용
        String encodedPassword = passwordEncoder.encode(newPassword);
        userRepository.updatePasswordByEmail(user.getEmail(), encodedPassword);
    }

    @Transactional
    public void cleanupExpiredTokens() {
        tokenRepository.deleteExpiredTokens(LocalDateTime.now());
        System.out.println("Expired password reset tokens cleaned up");
    }
}