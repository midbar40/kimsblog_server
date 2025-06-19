package com.unknown.kimsblog.controller;

import com.unknown.kimsblog.dto.AddUserRequest;
import com.unknown.kimsblog.dto.LoginRequest;
import com.unknown.kimsblog.dto.UserResponse;
import com.unknown.kimsblog.model.User;
import com.unknown.kimsblog.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class UserApiController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    // ✅ 회원가입 (간소화)
    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signup(@Valid @RequestBody AddUserRequest request) {
        userService.save(request);
        return ResponseEntity.ok(Map.of("message", "Signup successful"));
    }

    // ✅ 로그인 (간소화)
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest loginRequest,
                                                      HttpServletRequest request) {
        try {
            // 인증 처리
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );
            
            // SecurityContext 설정 및 세션 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);
            HttpSession session = request.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

            // 사용자 정보 반환
            User user = (User) authentication.getPrincipal();
            return ResponseEntity.ok(Map.of(
                "message", "Login successful",
                "user", createUserResponse(user)
            ));

        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Invalid email or password"));
        }
    }

    // ✅ 인증 상태 확인 (간소화)
    @GetMapping("/auth/status")
    public ResponseEntity<Map<String, Object>> getAuthStatus() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // 인증 검증
        if (!isValidAuthentication(authentication)) {
            return ResponseEntity.ok(Map.of("authenticated", false));
        }
        
        // 사용자 정보 반환
        try {
            User user = (User) authentication.getPrincipal();
            return ResponseEntity.ok(Map.of(
                "authenticated", true,
                "user", createUserResponse(user)
            ));
        } catch (ClassCastException e) {
            return ResponseEntity.ok(Map.of("authenticated", false));
        }
    }

    // ✅ 로그아웃 (간소화)
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        // 인증 정보 정리
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, null, authentication);
        }
        
        // 세션 정리
        Optional.ofNullable(request.getSession(false))
                .ifPresent(HttpSession::invalidate);

        return ResponseEntity.ok(Map.of("message", "Logout successful"));
    }

    // 🔧 Helper 메서드들
    private boolean isValidAuthentication(Authentication authentication) {
        return authentication != null && 
               authentication.isAuthenticated() && 
               !"anonymousUser".equals(authentication.getPrincipal()) &&
               !authentication.getClass().getSimpleName().equals("AnonymousAuthenticationToken");
    }

    private UserResponse createUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setNickname(user.getNickname());
        response.setRole(user.getRole());
        return response;
    }
}