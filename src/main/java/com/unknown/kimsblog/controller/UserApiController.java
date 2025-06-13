package com.unknown.kimsblog.controller;

import com.unknown.kimsblog.dto.AddUserRequest;
import com.unknown.kimsblog.dto.LoginRequest;
import com.unknown.kimsblog.model.User;
import com.unknown.kimsblog.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import com.unknown.kimsblog.dto.UserResponse;

import java.util.HashMap;
import java.util.Map;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api")

//@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})  // ✨ 특정 컨트롤러에 CORS 설정
public class UserApiController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody AddUserRequest request) {
        System.out.println("Received request:");
        System.out.println("Email: " + request.getEmail());
        System.out.println("Nickname: " + request.getNickname());
        System.out.println("Password: " + (request.getPassword() != null ? "***" : "null"));
        userService.save(request);
        return ResponseEntity.ok().body("Signup successful");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());

        try {
            Authentication authentication = authenticationManager.authenticate(authToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 세션 생성 (기본적으로 스프링 시큐리티가 처리)
            HttpSession session = request.getSession(true);

            return ResponseEntity.ok("Login successful");

        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login failed: " + ex.getMessage());
        }
    }

    @GetMapping("/auth/status")
    public ResponseEntity<?> getAuthStatus(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser")) {
            // 인증되지 않은 상태
            return ResponseEntity.ok(Map.of("authenticated", false));
        }

        // 인증된 사용자 정보 반환
        User user = (User) authentication.getPrincipal();

        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setEmail(user.getEmail());
        userResponse.setNickname(user.getNickname());
        userResponse.setRole(user.getRole()); // ADMIN 또는 USER

        Map<String, Object> response = new HashMap<>();
        response.put("authenticated", true);
        response.put("user", userResponse);

        return ResponseEntity.ok(response);
    }
}
