package com.unknown.kimsblog.controller;

import com.unknown.kimsblog.dto.AddUserRequest;
import com.unknown.kimsblog.dto.LoginRequest;
import com.unknown.kimsblog.model.User;
import com.unknown.kimsblog.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import com.unknown.kimsblog.dto.UserResponse;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class UserApiController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody AddUserRequest request) {
        System.out.println("Received signup request:");
        System.out.println("Email: " + request.getEmail());
        System.out.println("Nickname: " + request.getNickname());
        userService.save(request);
        return ResponseEntity.ok().body("Signup successful");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest,
                                   HttpServletRequest request,
                                   HttpServletResponse response) {
        System.out.println("=== Login Attempt ===");
        System.out.println("Email: " + loginRequest.getEmail());

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());

        try {
            Authentication authentication = authenticationManager.authenticate(authToken);

            // SecurityContext에 인증 정보 설정
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 세션 생성 및 SecurityContext 저장
            HttpSession session = request.getSession(true);

            // 중요: SecurityContext를 세션에 명시적으로 저장
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

            System.out.println("Session created: " + session.getId());
            System.out.println("Session creation time: " + session.getCreationTime());
            System.out.println("Session max inactive interval: " + session.getMaxInactiveInterval());
            System.out.println("Authentication set: " + authentication.isAuthenticated());
            System.out.println("Principal: " + authentication.getPrincipal().getClass().getSimpleName());

            // 세션에 SecurityContext가 저장되었는지 확인
            Object storedContext = session.getAttribute("SPRING_SECURITY_CONTEXT");
            System.out.println("SecurityContext stored in session: " + (storedContext != null));

            // 로그인 성공 시 사용자 정보도 함께 반환
            User user = (User) authentication.getPrincipal();

            UserResponse userResponse = new UserResponse();
            userResponse.setId(user.getId());
            userResponse.setEmail(user.getEmail());
            userResponse.setNickname(user.getNickname());
            userResponse.setRole(user.getRole());

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("message", "Login successful");
            responseBody.put("user", userResponse);
            responseBody.put("sessionId", session.getId()); // 디버깅용

            System.out.println("Login successful for: " + user.getEmail());
            return ResponseEntity.ok(responseBody);

        } catch (AuthenticationException ex) {
            System.out.println("Login failed: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login failed: " + ex.getMessage());
        }
    }

    @GetMapping("/auth/status")
    public ResponseEntity<?> getAuthStatus(HttpServletRequest request) {
        System.out.println("=== Auth Status Check ===");

        // 1. 쿠키에서 JSESSIONID 확인
        Cookie[] cookies = request.getCookies();
        String jsessionId = null;

        if (cookies != null) {
            System.out.println("🔍 받은 쿠키 목록:");
            for (Cookie cookie : cookies) {
                System.out.printf("- %s = %s%n", cookie.getName(), cookie.getValue());
                if ("JSESSIONID".equals(cookie.getName())) {
                    jsessionId = cookie.getValue();
                }
            }
        } else {
            System.out.println("🔍 쿠키 없음");
        }

        // 2. 세션 정보 확인 (false로 기존 세션만 가져오기)
        HttpSession session = request.getSession(false);
        System.out.println("JSESSIONID from cookie: " + jsessionId);
        System.out.println("Session exists: " + (session != null));

        if (session != null) {
            System.out.println("Session ID: " + session.getId());
            System.out.println("Session creation time: " + session.getCreationTime());
            System.out.println("Session last accessed: " + session.getLastAccessedTime());
            System.out.println("Session max inactive: " + session.getMaxInactiveInterval());
            System.out.println("Session is new: " + session.isNew());

            // 세션에서 Spring Security Context 확인
            Object springSecurityContext = session.getAttribute("SPRING_SECURITY_CONTEXT");
            System.out.println("Spring Security Context in session: " + (springSecurityContext != null));
        }

        // 3. SecurityContextHolder에서 인증 정보 확인
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null ? authentication.isAuthenticated() : "null"));
        System.out.println("Principal: " + (authentication != null ? authentication.getPrincipal() : "null"));
        System.out.println("Principal class: " + (authentication != null && authentication.getPrincipal() != null ?
                authentication.getPrincipal().getClass() : "null"));

        // 4. 인증 상태 확인 (AnonymousAuthenticationToken 체크 추가)
        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal()) ||
                authentication.getClass().getSimpleName().equals("AnonymousAuthenticationToken")) {

            System.out.println("User not authenticated");

            // 세션은 있지만 인증 정보가 없는 경우 추가 정보 제공
            Map<String, Object> response = new HashMap<>();
            response.put("authenticated", false);

            if (session != null) {
                response.put("reason", "Session exists but no valid authentication found");
                response.put("sessionId", session.getId());
            } else {
                response.put("reason", "No session found");
            }

            return ResponseEntity.ok(response);
        }

        try {
            // 5. 인증된 사용자 정보 반환
            User user = (User) authentication.getPrincipal();
            System.out.println("Authenticated user: " + user.getEmail());

            UserResponse userResponse = new UserResponse();
            userResponse.setId(user.getId());
            userResponse.setEmail(user.getEmail());
            userResponse.setNickname(user.getNickname());
            userResponse.setRole(user.getRole());

            Map<String, Object> response = new HashMap<>();
            response.put("authenticated", true);
            response.put("user", userResponse);

            if (session != null) {
                response.put("sessionId", session.getId());
            }

            System.out.println("Sending authenticated response for: " + user.getEmail());
            return ResponseEntity.ok(response);

        } catch (ClassCastException e) {
            System.out.println("Error casting principal to User: " + e.getMessage());
            return ResponseEntity.ok(Map.of(
                    "authenticated", false,
                    "reason", "Principal is not a User object: " + authentication.getPrincipal().getClass().getSimpleName()
            ));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        System.out.println("=== Logout Request ===");

        HttpSession session = request.getSession(false);
        if (session != null) {
            System.out.println("Current session ID: " + session.getId());
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            System.out.println("Logging out user: " + authentication.getName());
            new SecurityContextLogoutHandler().logout(request, null, authentication);
        }

        if (session != null) {
            System.out.println("Invalidating session: " + session.getId());
            session.invalidate();
        }

        System.out.println("Logout completed");
        return ResponseEntity.ok(Map.of("message", "Logout successful"));
    }
}