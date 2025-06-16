// UserStats.java
package com.unknown.kimsblog.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "total_played", nullable = false)
    private Integer totalPlayed = 0;

    @Column(name = "correct_answers", nullable = false)
    private Integer correctAnswers = 0;

    @Column(name = "current_streak", nullable = false)
    private Integer currentStreak = 0;

    @Column(name = "best_streak", nullable = false)
    private Integer bestStreak = 0;

    @Column(name = "total_time_spent", nullable = false)
    private Integer totalTimeSpent = 0;

    // 정답률 계산
    public double getAccuracyRate() {
        if (totalPlayed == 0) return 0.0;
        return (double) correctAnswers / totalPlayed * 100;
    }

    // 평균 답변 시간 계산
    public double getAverageTimePerQuestion() {
        if (totalPlayed == 0) return 0.0;
        return (double) totalTimeSpent / totalPlayed;
    }
}