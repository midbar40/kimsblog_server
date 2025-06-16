// QuizResultRepository.java
package com.unknown.kimsblog.repository;

import com.unknown.kimsblog.model.Quiz;
import com.unknown.kimsblog.model.QuizResult;
import com.unknown.kimsblog.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {

    // 사용자별 퀴즈 결과 조회
    Page<QuizResult> findByUser(User user, Pageable pageable);

    // 특정 퀴즈의 모든 결과 조회
    Page<QuizResult> findByQuiz(Quiz quiz, Pageable pageable);

    // 사용자가 특정 퀴즈를 풀었는지 확인
    Optional<QuizResult> findByUserAndQuiz(User user, Quiz quiz);

    // 사용자의 정답 개수
    long countByUserAndIsCorrect(User user, Boolean isCorrect);

    // 사용자의 총 문제 풀이 횟수
    long countByUser(User user);

    // 특정 퀴즈의 정답률
    @Query("SELECT AVG(CASE WHEN qr.isCorrect = true THEN 1.0 ELSE 0.0 END) " +
            "FROM QuizResult qr WHERE qr.quiz = :quiz")
    Double getAccuracyRateByQuiz(@Param("quiz") Quiz quiz);

    // 사용자의 최근 결과들 (연속 정답 계산용)
    List<QuizResult> findByUserOrderByAnsweredAtDesc(User user);

    // 사용자의 최근 N개 결과
    List<QuizResult> findTop10ByUserOrderByAnsweredAtDesc(User user);

    // 특정 기간 내 사용자 결과
    List<QuizResult> findByUserAndAnsweredAtBetween(User user, LocalDateTime start, LocalDateTime end);

    // 특정 퀴즈의 평균 소요 시간
    @Query("SELECT AVG(qr.timeTaken) FROM QuizResult qr WHERE qr.quiz = :quiz")
    Double getAverageTimeByQuiz(@Param("quiz") Quiz quiz);

    // 사용자의 카테고리별 정답률
    @Query("SELECT q.category, AVG(CASE WHEN qr.isCorrect = true THEN 1.0 ELSE 0.0 END) " +
            "FROM QuizResult qr JOIN qr.quiz q WHERE qr.user = :user GROUP BY q.category")
    List<Object[]> getCategoryAccuracyByUser(@Param("user") User user);

    // 퀴즈별 통계 업데이트를 위한 집계 쿼리
    @Query("SELECT COUNT(qr), SUM(CASE WHEN qr.isCorrect = true THEN 1 ELSE 0 END) " +
            "FROM QuizResult qr WHERE qr.quiz = :quiz")
    Object[] getQuizStatistics(@Param("quiz") Quiz quiz);
}
