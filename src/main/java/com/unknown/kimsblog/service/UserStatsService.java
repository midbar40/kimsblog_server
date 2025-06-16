// UserStatsService.java
package com.unknown.kimsblog.service;

import com.unknown.kimsblog.dto.UserStatsDto;
import com.unknown.kimsblog.model.User;
import com.unknown.kimsblog.model.UserStats;
import com.unknown.kimsblog.repository.UserStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserStatsService {

    private final UserStatsRepository userStatsRepository;

    // 사용자 통계 조회
    public UserStatsDto getUserStats(User user) {
        UserStats stats = userStatsRepository.findByUser(user)
                .orElseGet(() -> createDefaultStats(user));

        return UserStatsDto.fromEntity(stats);
    }

    // 정답률 상위 사용자들 (리더보드)
    public List<UserStatsDto> getTopUsersByAccuracy(int minPlayed, int limit) {
        return userStatsRepository.findTopUsersByAccuracy(minPlayed)
                .stream()
                .limit(limit)
                .map(UserStatsDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 최고 연속 정답 상위 사용자들
    public List<UserStatsDto> getTopUsersByStreak() {
        return userStatsRepository.findTop10ByOrderByBestStreakDesc()
                .stream()
                .map(UserStatsDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 총 플레이 횟수 상위 사용자들
    public List<UserStatsDto> getTopUsersByPlayCount() {
        return userStatsRepository.findTop10ByOrderByTotalPlayedDesc()
                .stream()
                .map(UserStatsDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 사용자 랭킹 조회
    public Long getUserRank(User user) {
        UserStats stats = userStatsRepository.findByUser(user)
                .orElse(null);

        if (stats == null || stats.getTotalPlayed() == 0) {
            return null;
        }

        return userStatsRepository.getUserRankByAccuracy(
                stats.getCorrectAnswers(),
                stats.getTotalPlayed(),
                5 // 최소 5문제 이상 푼 사용자들과 비교
        );
    }

    // 전체 평균 통계
    public UserStatsDto getGlobalAverageStats() {
        Object[] avgStats = userStatsRepository.getGlobalAverageStats();

        UserStatsDto dto = new UserStatsDto();
        if (avgStats[0] != null) {
            dto.setTotalPlayed(((Double) avgStats[0]).intValue());
            dto.setAccuracyRate((Double) avgStats[1] * 100);
            dto.setBestStreak(((Double) avgStats[2]).intValue());
        }

        return dto;
    }

    // 기본 통계 생성
    @Transactional
    private UserStats createDefaultStats(User user) {
        UserStats stats = new UserStats();
        stats.setUser(user);
        return userStatsRepository.save(stats);
    }
}