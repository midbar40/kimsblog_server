// QuizRepository.java
package com.unknown.kimsblog.repository;

import com.unknown.kimsblog.model.Quiz;
import com.unknown.kimsblog.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {

    // 카테고리별 퀴즈 조회
    Page<Quiz> findByCategory(String category, Pageable pageable);

    // 난이도별 퀴즈 조회
    Page<Quiz> findByDifficulty(Quiz.Difficulty difficulty, Pageable pageable);

    // 카테고리와 난이도로 필터링
    Page<Quiz> findByCategoryAndDifficulty(String category, Quiz.Difficulty difficulty, Pageable pageable);

    // 제작자별 퀴즈 조회
    Page<Quiz> findByCreatedBy(User createdBy, Pageable pageable);

    // 키워드로 문제 검색 (문제 내용에서 검색)
    @Query("SELECT q FROM Quiz q WHERE q.question LIKE %:keyword%")
    Page<Quiz> findByQuestionContaining(@Param("keyword") String keyword, Pageable pageable);

    // 복합 검색 (카테고리, 난이도, 키워드)
    @Query("SELECT q FROM Quiz q WHERE " +
            "(:category IS NULL OR q.category = :category) AND " +
            "(:difficulty IS NULL OR q.difficulty = :difficulty) AND " +
            "(:keyword IS NULL OR q.question LIKE %:keyword%)")
    Page<Quiz> findByFilters(@Param("category") String category,
                             @Param("difficulty") Quiz.Difficulty difficulty,
                             @Param("keyword") String keyword,
                             Pageable pageable);

    // 인기 퀴즈 조회 (플레이 횟수 기준)
    @Query("SELECT q FROM Quiz q ORDER BY q.playCount DESC")
    Page<Quiz> findPopularQuizzes(Pageable pageable);

    // 최신 퀴즈 조회
    Page<Quiz> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // 카테고리 목록 조회
    @Query("SELECT DISTINCT q.category FROM Quiz q ORDER BY q.category")
    List<String> findDistinctCategories();

    // 랜덤 퀴즈 조회
    @Query(value = "SELECT * FROM quizzes ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Optional<Quiz> findRandomQuiz();

    // 사용자가 아직 풀지 않은 퀴즈 조회
    @Query("SELECT q FROM Quiz q WHERE q.id NOT IN " +
            "(SELECT qr.quiz.id FROM QuizResult qr WHERE qr.user = :user)")
    Page<Quiz> findUnsolvedQuizzesByUser(@Param("user") User user, Pageable pageable);

    // 특정 카테고리의 퀴즈 개수
    long countByCategory(String category);

    // 특정 사용자가 만든 퀴즈 개수
    long countByCreatedBy(User createdBy);
}