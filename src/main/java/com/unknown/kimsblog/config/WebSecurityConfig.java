// WebSecurityConfig.java (수정된 버전)
package com.unknown.kimsblog.config;

import com.unknown.kimsblog.service.UserDetailService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final UserDetailService userDetailService;

    // 환경변수에서 프론트엔드 URL 주입
    @Value("${FRONTEND_URL}")
    private String frontendUrl;

    @Value("${VERCEL_PATTERN}")
    private String vercelPattern;

    @Value("${GIT_PATTERN}")
    private String gitPattern;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)

                // 세션 관리 설정 - .and() 제거하고 체이닝 방식으로 변경
                .sessionManagement(session -> {
                    session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                            .maximumSessions(1)
                            .maxSessionsPreventsLogin(false)
                            .sessionRegistry(null); // 필요시 SessionRegistry 설정
                    session.sessionFixation().migrateSession()
                            .invalidSessionUrl("/api/auth/status");
                })

                // SecurityContext를 세션에 저장하도록 명시적 설정
                .securityContext(context -> context
                        .securityContextRepository(securityContextRepository()))

                .authorizeHttpRequests(auth -> auth
                        // 정적 파일 및 기본 페이지
                        .requestMatchers("/static/**").permitAll()
                        .requestMatchers("/login", "/signup", "/api/login", "/api/signup").permitAll()
                        .requestMatchers("/api/auth/status").permitAll()
                        .requestMatchers("/api/logout").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 비밀번호 재설정 관련 엔드포인트
                        .requestMatchers("/api/password/forgot").permitAll()
                        .requestMatchers("/api/password/validate-token").permitAll()
                        .requestMatchers("/api/password/reset").permitAll()
                        .requestMatchers("/forgot-password", "/reset-password").permitAll()

                        // 댓글 관련 API - 모든 댓글 작업을 로그인 없이 허용
                        .requestMatchers("/api/posts/*/comments", "/api/posts/*/comments/**").permitAll()
                        .requestMatchers("/api/comments/**").permitAll()

                        // 게시글 관련 API
                        .requestMatchers(HttpMethod.GET, "/api/posts", "/api/posts/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/posts/paged").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/posts/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/posts/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/posts/**").authenticated()

                        // 퀴즈 관련 API 권한 설정
                        // 퀴즈 조회는 모든 사용자에게 허용 (로그인 없이도 볼 수 있음)
                        // .requestMatchers(HttpMethod.GET, "/api/quiz", "/api/quiz/**").permitAll()
                        // .requestMatchers(HttpMethod.GET, "/api/quiz/by-category").permitAll()
                        // .requestMatchers(HttpMethod.GET, "/api/quiz/categories").permitAll()
                        // .requestMatchers(HttpMethod.GET, "/api/quiz/random").permitAll()
                        // .requestMatchers(HttpMethod.GET, "/api/quiz/popular").permitAll()
                        // .requestMatchers(HttpMethod.GET, "/api/quiz/latest").permitAll()
                        // .requestMatchers(HttpMethod.GET, "/api/quiz/*/play").permitAll()

                        // 퀴즈 생성, 수정, 삭제, 답안 제출은 인증 필요
                        .requestMatchers(HttpMethod.POST, "/api/quiz").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/quiz/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/quiz/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/quiz/submit").authenticated()
                        .requestMatchers("/api/quiz/my-quizzes").authenticated()
                        .requestMatchers("/api/quiz/my-results").authenticated()
                        .requestMatchers("/api/quiz/unsolved").authenticated()

                        // 통계 관련 API
                        .requestMatchers(HttpMethod.GET, "/api/stats/global").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/stats/leaderboard/**").permitAll()
                        .requestMatchers("/api/stats/me").authenticated()

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
        // 디버깅용 로그 추가
        System.out.println("=== CORS Configuration Debug ===");
        System.out.println("FRONTEND_URL: " + frontendUrl);
        System.out.println("VERCEL_PATTERN: " + vercelPattern);
        System.out.println("GIT_PATTERN: " + gitPattern);
        
        CorsConfiguration config = new CorsConfiguration();

        // 환경변수에서 받은 프론트엔드 URL 사용
        // 개발환경에서는 localhost의 모든 포트, 운영환경에서는 정확한 도메인
        if (frontendUrl.contains("localhost")) {
            // 로컬 개발환경: localhost의 모든 포트 허용
            config.setAllowedOriginPatterns(List.of("http://localhost:*", "https://localhost:*"));
        } else {
            // 운영환경: 정확한 도메인만 허용
            config.setAllowedOrigins(List.of(
                    frontendUrl,
                    vercelPattern,
                    gitPattern));
        }

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        // 노출할 헤더 설정 (필요시)
        config.setExposedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

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