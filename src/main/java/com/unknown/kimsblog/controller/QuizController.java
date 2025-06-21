// QuizController.java
package com.unknown.kimsblog.controller;

import com.unknown.kimsblog.dto.*;
import com.unknown.kimsblog.model.User;
import com.unknown.kimsblog.service.QuizService;
// import com.unknown.kimsblog.service.UserStatsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;
    // private final UserStatsService userStatsService;

    // 퀴즈 생성
    @PostMapping
    public ResponseEntity<?> createQuiz(@Valid @RequestBody CreateQuizRequest request,
                                        Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            QuizDto quiz = quizService.createQuiz(request, user);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "퀴즈가 성공적으로 생성되었습니다");
            response.put("quiz", quiz);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 퀴즈 목록 조회 (검색 및 필터링)
    @GetMapping
    public ResponseEntity<?> getQuizzes(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        try {
            QuizSearchRequest searchRequest = new QuizSearchRequest();
            searchRequest.setCategory(category);
            searchRequest.setDifficulty(difficulty);
            searchRequest.setKeyword(keyword);
            searchRequest.setSortBy(sortBy);
            searchRequest.setSortDirection(sortDirection);
            searchRequest.setPage(page);
            searchRequest.setSize(size);

            Page<QuizDto> quizzes = quizService.getQuizzes(searchRequest);
            return ResponseEntity.ok(quizzes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 카테고리별 퀴즈 그룹화 조회
    @GetMapping("/by-category")
    public ResponseEntity<?> getQuizzesByCategory() {
        try {
            Map<String, List<QuizDto>> quizzesByCategory = quizService.getQuizzesByCategory();
            return ResponseEntity.ok(quizzesByCategory);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 카테고리 목록 조회
    @GetMapping("/categories")
    public ResponseEntity<?> getCategories() {
        try {
            List<String> categories = quizService.getCategories();
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 랜덤 퀴즈 조회
    @GetMapping("/random")
    public ResponseEntity<?> getRandomQuiz() {
        try {
            QuizDto quiz = quizService.getRandomQuiz();
            return ResponseEntity.ok(quiz);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 인기 퀴즈 조회
    @GetMapping("/popular")
    public ResponseEntity<?> getPopularQuizzes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<QuizDto> quizzes = quizService.getPopularQuizzes(page, size);
            return ResponseEntity.ok(quizzes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 최신 퀴즈 조회
    @GetMapping("/latest")
    public ResponseEntity<?> getLatestQuizzes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<QuizDto> quizzes = quizService.getLatestQuizzes(page, size);
            return ResponseEntity.ok(quizzes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 특정 퀴즈 조회 (플레이용)
    @GetMapping("/{id}/play")
    public ResponseEntity<?> getQuizForPlay(@PathVariable Long id) {
        try {
            QuizDto quiz = quizService.getQuizForPlay(id);
            return ResponseEntity.ok(quiz);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 퀴즈 상세 정보 조회 (제작자용)
    @GetMapping("/{id}")
    public ResponseEntity<?> getQuizDetail(@PathVariable Long id, Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            QuizDto quiz = quizService.getQuizDetail(id, user);
            return ResponseEntity.ok(quiz);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 답안 제출
    @PostMapping("/submit")
    public ResponseEntity<?> submitAnswer(@Valid @RequestBody SubmitAnswerRequest request,
                                          Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            QuizResultDto result = quizService.submitAnswer(request, user);

            Map<String, Object> response = new HashMap<>();
            response.put("result", result);
            response.put("message", result.getIsCorrect() ? "정답입니다!" : "틀렸습니다");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 퀴즈 수정
    @PutMapping("/{id}")
    public ResponseEntity<?> updateQuiz(@PathVariable Long id,
                                        @Valid @RequestBody CreateQuizRequest request,
                                        Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            QuizDto quiz = quizService.updateQuiz(id, request, user);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "퀴즈가 성공적으로 수정되었습니다");
            response.put("quiz", quiz);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 퀴즈 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteQuiz(@PathVariable Long id, Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            quizService.deleteQuiz(id, user);

            return ResponseEntity.ok(Map.of("message", "퀴즈가 성공적으로 삭제되었습니다"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 사용자가 만든 퀴즈 조회
    @GetMapping("/my-quizzes")
    public ResponseEntity<?> getMyQuizzes(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size,
                                          Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            Page<QuizDto> quizzes = quizService.getUserCreatedQuizzes(user, page, size);
            return ResponseEntity.ok(quizzes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 사용자 퀴즈 결과 조회
    @GetMapping("/my-results")
    public ResponseEntity<?> getMyResults(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size,
                                          Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            Page<QuizResultDto> results = quizService.getUserResults(user, page, size);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 사용자가 풀지 않은 퀴즈 조회
    @GetMapping("/unsolved")
    public ResponseEntity<?> getUnsolvedQuizzes(@RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size,
                                                Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            Page<QuizDto> quizzes = quizService.getUnsolvedQuizzes(user, page, size);
            return ResponseEntity.ok(quizzes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
