// WebSecurityConfig.java (완전한 버전 - 하드코딩)
package com.unknown.kimsblog.config;

import com.unknown.kimsblog.service.UserDetailService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final UserDetailService userDetailService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)

                // 세션 관리 설정
                .sessionManagement(session -> {
                    session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                            .maximumSessions(1)
                            .maxSessionsPreventsLogin(false);
                    session.sessionFixation().migrateSession()
                            .invalidSessionUrl("/api/auth/status");
                })

                // SecurityContext를 세션에 저장하도록 명시적 설정
                .securityContext(context -> context
                        .securityContextRepository(securityContextRepository()))

                .authorizeHttpRequests(auth -> auth
                        // ===========================================
                        // 🚨 공개 API - 인증 없이 접근 가능
                        // ===========================================
                        
                        // 디버그 및 헬스체크
                        .requestMatchers("/api/debug/**").permitAll()
                        .requestMatchers("/api/health").permitAll()
                        
                        // OPTIONS 요청 (CORS preflight) - 최우선
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        
                        // 정적 파일 및 기본 페이지
                        .requestMatchers("/static/**").permitAll()
                        .requestMatchers("/login", "/signup").permitAll()
                        
                        // 인증 관련 API
                        .requestMatchers("/api/login", "/api/signup").permitAll()
                        .requestMatchers("/api/auth/status").permitAll()
                        .requestMatchers("/api/logout").permitAll()

                        // 비밀번호 재설정 관련 엔드포인트
                        .requestMatchers("/api/password/**").permitAll()
                        .requestMatchers("/forgot-password", "/reset-password").permitAll()

                        // ===========================================
                        // 📝 게시글 관련 API - 구체적인 경로부터
                        // ===========================================
                        .requestMatchers(HttpMethod.GET, "/api/posts/paged").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/posts").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/posts/**").permitAll()
                        
                        // 게시글 작성/수정/삭제는 인증 필요
                        .requestMatchers(HttpMethod.POST, "/api/posts").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/posts/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/posts/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/posts/**").authenticated()

                        // ===========================================
                        // 💬 댓글 관련 API (모든 작업 공개)
                        // ===========================================
                        .requestMatchers("/api/posts/*/comments").permitAll()
                        .requestMatchers("/api/posts/*/comments/**").permitAll()
                        .requestMatchers("/api/comments/**").permitAll()

                        // ===========================================
                        // 🧩 퀴즈 관련 API
                        // ===========================================
                        .requestMatchers(HttpMethod.GET, "/api/quiz/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/quiz").permitAll()
                        
                        // 퀴즈 생성, 수정, 삭제, 답안 제출은 인증 필요
                        .requestMatchers(HttpMethod.POST, "/api/quiz").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/quiz/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/quiz/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/quiz/submit").authenticated()
                        .requestMatchers("/api/quiz/my-quizzes").authenticated()
                        .requestMatchers("/api/quiz/my-results").authenticated()
                        .requestMatchers("/api/quiz/unsolved").authenticated()

                        // ===========================================
                        // 📊 통계 관련 API
                        // ===========================================
                        .requestMatchers(HttpMethod.GET, "/api/stats/global").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/stats/leaderboard/**").permitAll()
                        .requestMatchers("/api/stats/me").authenticated()

                        // 나머지는 인증 필요
                        .anyRequest().authenticated())

                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                .logout(logout -> logout
                        .logoutUrl("/api/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            System.out.println("=== Logout Success Handler ===");
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.getWriter().write("{\"message\":\"Logout successful\"}");
                            response.setContentType("application/json");
                        })
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .clearAuthentication(true)
                        .permitAll())

                // 예외 처리 핸들러
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            System.out.println("=== Authentication Entry Point ===");
                            System.out.println("Unauthorized access to: " + request.getRequestURI());
                            System.out.println("Method: " + request.getMethod());
                            System.out.println("Origin: " + request.getHeader("Origin"));
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("{\"error\":\"Authentication required\"}");
                            response.setContentType("application/json");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            System.out.println("=== Access Denied Handler ===");
                            System.out.println("Access denied to: " + request.getRequestURI());
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.getWriter().write("{\"error\":\"Access denied\"}");
                            response.setContentType("application/json");
                        }))

                .build();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        HttpSessionSecurityContextRepository repository = new HttpSessionSecurityContextRepository();
        repository.setAllowSessionCreation(true);
        repository.setDisableUrlRewriting(true);
        repository.setSpringSecurityContextKey("SPRING_SECURITY_CONTEXT");
        return repository;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        System.out.println("=== CORS Configuration (하드코딩 버전) ===");
        
        CorsConfiguration config = new CorsConfiguration();

        // 🎯 하드코딩된 허용 URL들 (환경변수 의존성 제거)
        config.setAllowedOrigins(Arrays.asList(
            // Vercel 도메인들
            "https://kimsblogfront.vercel.app",
            "https://kimsblogfront-seunghyuns-projects-1b045e8e.vercel.app",
            "https://kimsblogfront-git-main-seunghyuns-projects-1b045e8e.vercel.app",
            
            // 개발환경
            "http://localhost:3000",
            "http://localhost:5173",
            "http://127.0.0.1:3000",
            "http://127.0.0.1:5173"
        ));

        // 🌟 추가로 패턴도 허용 (Vercel 자동 생성 URL 대응)
        config.setAllowedOriginPatterns(Arrays.asList(
            "https://kimsblogfront-*.vercel.app",
            "https://kimsblogfront-git-*.vercel.app"
        ));

        config.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"
        ));
        
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        // 노출할 헤더 설정
        config.setExposedHeaders(Arrays.asList(
            "Authorization", 
            "Cache-Control", 
            "Content-Type",
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Methods",
            "Access-Control-Allow-Headers"
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        System.out.println("CORS 허용 Origin:");
        System.out.println("- https://kimsblogfront.vercel.app");
        System.out.println("- https://kimsblogfront-*.vercel.app");
        System.out.println("- http://localhost:3000");
        System.out.println("CORS 설정 완료!");

        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailService);
        provider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(provider);
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}