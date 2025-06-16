// QuizService.java
package com.unknown.kimsblog.service;

import com.unknown.kimsblog.dto.*;
import com.unknown.kimsblog.model.Quiz;
import com.unknown.kimsblog.model.QuizResult;
import com.unknown.kimsblog.model.User;
import com.unknown.kimsblog.model.UserStats;
import com.unknown.kimsblog.repository.QuizRepository;
import com.unknown.kimsblog.repository.QuizResultRepository;
import com.unknown.kimsblog.repository.UserStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizResultRepository quizResultRepository;
    private final UserStatsRepository userStatsRepository;

    // 퀴즈 생성
    @Transactional
    public QuizDto createQuiz(CreateQuizRequest request, User creator) {
        Quiz quiz = new Quiz();
        quiz.setQuestion(request.getQuestion());
        quiz.setAnswer(request.getAnswer());
        quiz.setTimeLimit(request.getTimeLimit());
        quiz.setCategory(request.getCategory());
        quiz.setDifficulty(Quiz.Difficulty.valueOf(request.getDifficulty().toUpperCase()));
        quiz.setCreatedBy(creator);

        Quiz savedQuiz = quizRepository.save(quiz);
        return QuizDto.fromEntity(savedQuiz);
    }

    // 퀴즈 목록 조회 (검색 및 필터링)
    public Page<QuizDto> getQuizzes(QuizSearchRequest searchRequest) {
        Pageable pageable = createPageable(searchRequest);

        Quiz.Difficulty difficulty = null;
        if (searchRequest.getDifficulty() != null && !searchRequest.getDifficulty().isEmpty()) {
            difficulty = Quiz.Difficulty.valueOf(searchRequest.getDifficulty().toUpperCase());
        }

        Page<Quiz> quizzes = quizRepository.findByFilters(
                searchRequest.getCategory(),
                difficulty,
                searchRequest.getKeyword(),
                pageable
        );

        return quizzes.map(QuizDto::fromEntity);
    }

    // 플레이용 퀴즈 조회 (정답 숨김)
    public QuizDto getQuizForPlay(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("퀴즈를 찾을 수 없습니다: " + quizId));

        return QuizDto.fromEntityForPlay(quiz);
    }

    // 랜덤 퀴즈 조회
    public QuizDto getRandomQuiz() {
        Quiz quiz = quizRepository.findRandomQuiz()
                .orElseThrow(() -> new IllegalStateException("사용 가능한 퀴즈가 없습니다"));

        return QuizDto.fromEntityForPlay(quiz);
    }

    // 답안 제출
    @Transactional
    public QuizResultDto submitAnswer(SubmitAnswerRequest request, User user) {
        Quiz quiz = quizRepository.findById(request.getQuizId())
                .orElseThrow(() -> new IllegalArgumentException("퀴즈를 찾을 수 없습니다: " + request.getQuizId()));

        // 답안 검증
        boolean isCorrect = quiz.getAnswer().trim().equalsIgnoreCase(request.getUserAnswer().trim());

        // 퀴즈 결과 저장
        QuizResult result = new QuizResult();
        result.setQuiz(quiz);
        result.setUser(user);
        result.setUserAnswer(request.getUserAnswer());
        result.setIsCorrect(isCorrect);
        result.setTimeTaken(request.getTimeTaken());

        QuizResult savedResult = quizResultRepository.save(result);

        // 퀴즈 통계 업데이트
        updateQuizStatistics(quiz, isCorrect);

        // 사용자 통계 업데이트
        updateUserStatistics(user, isCorrect, request.getTimeTaken());

        return QuizResultDto.fromEntity(savedResult);
    }

    // 사용자의 퀴즈 결과 조회
    public Page<QuizResultDto> getUserResults(User user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "answeredAt"));
        Page<QuizResult> results = quizResultRepository.findByUser(user, pageable);
        return results.map(QuizResultDto::fromEntity);
    }

    // 카테고리별 퀴즈 그룹화
    public Map<String, List<QuizDto>> getQuizzesByCategory() {
        List<Quiz> allQuizzes = quizRepository.findAll();
        return allQuizzes.stream()
                .map(QuizDto::fromEntity)
                .collect(Collectors.groupingBy(QuizDto::getCategory));
    }

    // 카테고리 목록 조회
    public List<String> getCategories() {
        return quizRepository.findDistinctCategories();
    }

    // 사용자가 만든 퀴즈 조회
    public Page<QuizDto> getUserCreatedQuizzes(User user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Quiz> quizzes = quizRepository.findByCreatedBy(user, pageable);
        return quizzes.map(QuizDto::fromEntity);
    }

    // 인기 퀴즈 조회
    public Page<QuizDto> getPopularQuizzes(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Quiz> quizzes = quizRepository.findPopularQuizzes(pageable);
        return quizzes.map(QuizDto::fromEntity);
    }

    // 최신 퀴즈 조회
    public Page<QuizDto> getLatestQuizzes(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Quiz> quizzes = quizRepository.findAllByOrderByCreatedAtDesc(pageable);
        return quizzes.map(QuizDto::fromEntity);
    }

    // 사용자가 풀지 않은 퀴즈 조회
    public Page<QuizDto> getUnsolvedQuizzes(User user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Quiz> quizzes = quizRepository.findUnsolvedQuizzesByUser(user, pageable);
        return quizzes.map(QuizDto::fromEntityForPlay);
    }

    // 퀴즈 상세 정보 조회 (제작자용 - 정답 포함)
    public QuizDto getQuizDetail(Long quizId, User user) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("퀴즈를 찾을 수 없습니다: " + quizId));

        // 제작자만 정답을 볼 수 있음
        if (quiz.getCreatedBy().getId().equals(user.getId())) {
            return QuizDto.fromEntity(quiz);
        } else {
            return QuizDto.fromEntityForPlay(quiz);
        }
    }

    // 퀴즈 삭제 (제작자만 가능)
    @Transactional
    public void deleteQuiz(Long quizId, User user) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("퀴즈를 찾을 수 없습니다: " + quizId));

        if (!quiz.getCreatedBy().getId().equals(user.getId())) {
            throw new IllegalArgumentException("퀴즈를 삭제할 권한이 없습니다");
        }

        quizRepository.delete(quiz);
    }

    // 퀴즈 수정 (제작자만 가능)
    @Transactional
    public QuizDto updateQuiz(Long quizId, CreateQuizRequest request, User user) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("퀴즈를 찾을 수 없습니다: " + quizId));

        if (!quiz.getCreatedBy().getId().equals(user.getId())) {
            throw new IllegalArgumentException("퀴즈를 수정할 권한이 없습니다");
        }

        quiz.setQuestion(request.getQuestion());
        quiz.setAnswer(request.getAnswer());
        quiz.setTimeLimit(request.getTimeLimit());
        quiz.setCategory(request.getCategory());
        quiz.setDifficulty(Quiz.Difficulty.valueOf(request.getDifficulty().toUpperCase()));

        Quiz updatedQuiz = quizRepository.save(quiz);
        return QuizDto.fromEntity(updatedQuiz);
    }

    // 퀴즈 통계 업데이트
    @Transactional
    private void updateQuizStatistics(Quiz quiz, boolean isCorrect) {
        quiz.setPlayCount(quiz.getPlayCount() + 1);
        if (isCorrect) {
            quiz.setCorrectCount(quiz.getCorrectCount() + 1);
        }
        quizRepository.save(quiz);
    }

    // 사용자 통계 업데이트
    @Transactional
    private void updateUserStatistics(User user, boolean isCorrect, int timeTaken) {
        UserStats stats = userStatsRepository.findByUser(user)
                .orElseGet(() -> {
                    UserStats newStats = new UserStats();
                    newStats.setUser(user);
                    return newStats;
                });

        stats.setTotalPlayed(stats.getTotalPlayed() + 1);
        stats.setTotalTimeSpent(stats.getTotalTimeSpent() + timeTaken);

        if (isCorrect) {
            stats.setCorrectAnswers(stats.getCorrectAnswers() + 1);
            stats.setCurrentStreak(stats.getCurrentStreak() + 1);
            if (stats.getCurrentStreak() > stats.getBestStreak()) {
                stats.setBestStreak(stats.getCurrentStreak());
            }
        } else {
            stats.setCurrentStreak(0);
        }

        userStatsRepository.save(stats);
    }

    // Pageable 생성 헬퍼 메서드
    private Pageable createPageable(QuizSearchRequest searchRequest) {
        Sort sort = Sort.by(
                searchRequest.getSortDirection().equalsIgnoreCase("asc")
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC,
                searchRequest.getSortBy()
        );

        return PageRequest.of(searchRequest.getPage(), searchRequest.getSize(), sort);
    }
}
