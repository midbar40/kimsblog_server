// UserStatsRepository.java
package com.unknown.kimsblog.repository;

import com.unknown.kimsblog.model.User;
import com.unknown.kimsblog.model.UserStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserStatsRepository extends JpaRepository<UserStats, Long> {

    // 사용자별 통계 조회
    Optional<UserStats> findByUser(User user);

    // 정답률 상위 사용자들
    @Query("SELECT us FROM UserStats us WHERE us.totalPlayed >= :minPlayed ORDER BY " +
            "(CAST(us.correctAnswers AS double) / us.totalPlayed) DESC")
    List<UserStats> findTopUsersByAccuracy(@Param("minPlayed") Integer minPlayed);

    // 최고 연속 정답 기록 상위 사용자들
    List<UserStats> findTop10ByOrderByBestStreakDesc();

    // 총 플레이 횟수 상위 사용자들
    List<UserStats> findTop10ByOrderByTotalPlayedDesc();

    // 전체 사용자 평균 통계
    @Query("SELECT AVG(us.totalPlayed), AVG(CAST(us.correctAnswers AS double) / us.totalPlayed), " +
            "AVG(us.bestStreak) FROM UserStats us WHERE us.totalPlayed > 0")
    Object[] getGlobalAverageStats();

    // 사용자 랭킹 조회 (정답률 기준)
    @Query("SELECT COUNT(us) + 1 FROM UserStats us WHERE " +
            "us.totalPlayed >= :minPlayed AND " +
            "(CAST(us.correctAnswers AS double) / us.totalPlayed) > " +
            "(CAST(:correctAnswers AS double) / :totalPlayed)")
    Long getUserRankByAccuracy(@Param("correctAnswers") Integer correctAnswers,
                               @Param("totalPlayed") Integer totalPlayed,
                               @Param("minPlayed") Integer minPlayed);
}