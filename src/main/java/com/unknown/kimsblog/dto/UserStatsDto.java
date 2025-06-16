// UserStatsDto.java
package com.unknown.kimsblog.dto;

import com.unknown.kimsblog.model.UserStats;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsDto {
    private Integer totalPlayed;
    private Integer correctAnswers;
    private Integer currentStreak;
    private Integer bestStreak;
    private Double accuracyRate;
    private Double averageTimePerQuestion;
    private Integer totalTimeSpent;

    public static UserStatsDto fromEntity(UserStats stats) {
        UserStatsDto dto = new UserStatsDto();
        dto.setTotalPlayed(stats.getTotalPlayed());
        dto.setCorrectAnswers(stats.getCorrectAnswers());
        dto.setCurrentStreak(stats.getCurrentStreak());
        dto.setBestStreak(stats.getBestStreak());
        dto.setAccuracyRate(stats.getAccuracyRate());
        dto.setAverageTimePerQuestion(stats.getAverageTimePerQuestion());
        dto.setTotalTimeSpent(stats.getTotalTimeSpent());
        return dto;
    }
}