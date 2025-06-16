package com.unknown.kimsblog.controller;

import com.unknown.kimsblog.dto.ForgotPasswordRequest;
import com.unknown.kimsblog.dto.ResetPasswordRequest;
import com.unknown.kimsblog.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/password")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/forgot")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        System.out.println("=== Forgot Password Request ===");
        System.out.println("Email: " + request.getEmail());

        try {
            passwordResetService.sendPasswordResetEmail(request.getEmail());

            return ResponseEntity.ok(Map.of(
                    "message", "이메일을 확인해주세요. 비밀번호 재설정 링크를 보내드렸습니다.",
                    "success", true
            ));

        } catch (Exception e) {
            System.err.println("Forgot password error: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "비밀번호 재설정 이메일 전송에 실패했습니다.",
                    "success", false
            ));
        }
    }

    @GetMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestParam String token) {
        System.out.println("=== Validate Reset Token ===");
        System.out.println("Token: " + token);

        boolean isValid = passwordResetService.validateResetToken(token);

        return ResponseEntity.ok(Map.of(
                "valid", isValid,
                "message", isValid ? "유효한 토큰입니다." : "유효하지 않거나 만료된 토큰입니다."
        ));
    }

    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        System.out.println("=== Reset Password Request ===");
        System.out.println("Token: " + request.getToken());

        // 비밀번호 확인 검증
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "새 비밀번호와 확인 비밀번호가 일치하지 않습니다.",
                    "success", false
            ));
        }

        try {
            passwordResetService.resetPassword(request.getToken(), request.getNewPassword());

            return ResponseEntity.ok(Map.of(
                    "message", "비밀번호가 성공적으로 변경되었습니다.",
                    "success", true
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", e.getMessage(),
                    "success", false
            ));

        } catch (Exception e) {
            System.err.println("Reset password error: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "비밀번호 변경에 실패했습니다.",
                    "success", false
            ));
        }
    }
}