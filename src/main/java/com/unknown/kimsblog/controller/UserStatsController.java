// UserStatsController.java
package com.unknown.kimsblog.controller;

import com.unknown.kimsblog.dto.UserStatsDto;
import com.unknown.kimsblog.model.User;
import com.unknown.kimsblog.service.UserStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class UserStatsController {

    private final UserStatsService userStatsService;

    // 내 통계 조회
    @GetMapping("/me")
    public ResponseEntity<?> getMyStats(Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            UserStatsDto stats = userStatsService.getUserStats(user);

            Map<String, Object> response = new HashMap<>();
            response.put("stats", stats);

            // 랭킹 정보도 함께 제공
            Long rank = userStatsService.getUserRank(user);
            if (rank != null) {
                response.put("rank", rank);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 정답률 리더보드
    @GetMapping("/leaderboard/accuracy")
    public ResponseEntity<?> getAccuracyLeaderboard(
            @RequestParam(defaultValue = "5") int minPlayed,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<UserStatsDto> leaderboard = userStatsService.getTopUsersByAccuracy(minPlayed, limit);
            return ResponseEntity.ok(leaderboard);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 연속 정답 리더보드
    @GetMapping("/leaderboard/streak")
    public ResponseEntity<?> getStreakLeaderboard() {
        try {
            List<UserStatsDto> leaderboard = userStatsService.getTopUsersByStreak();
            return ResponseEntity.ok(leaderboard);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 플레이 횟수 리더보드
    @GetMapping("/leaderboard/playcount")
    public ResponseEntity<?> getPlayCountLeaderboard() {
        try {
            List<UserStatsDto> leaderboard = userStatsService.getTopUsersByPlayCount();
            return ResponseEntity.ok(leaderboard);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 전체 평균 통계
    @GetMapping("/global")
    public ResponseEntity<?> getGlobalStats() {
        try {
            UserStatsDto globalStats = userStatsService.getGlobalAverageStats();
            return ResponseEntity.ok(globalStats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}