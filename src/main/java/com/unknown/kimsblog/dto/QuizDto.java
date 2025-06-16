// QuizDto.java
package com.unknown.kimsblog.dto;

import com.unknown.kimsblog.model.Quiz;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuizDto {
    private Long id;
    private String question;
    private String answer;
    private Integer timeLimit;
    private String category;
    private String difficulty;
    private String createdBy;
    private String createdAt;
    private Integer playCount;
    private Integer correctCount;
    private Double accuracyRate;

    public static QuizDto fromEntity(Quiz quiz) {
        QuizDto dto = new QuizDto();
        dto.setId(quiz.getId());
        dto.setQuestion(quiz.getQuestion());
        dto.setAnswer(quiz.getAnswer());
        dto.setTimeLimit(quiz.getTimeLimit());
        dto.setCategory(quiz.getCategory());
        dto.setDifficulty(quiz.getDifficulty().name().toLowerCase());
        dto.setCreatedBy(quiz.getCreatedBy().getNickname());
        dto.setCreatedAt(quiz.getCreatedAt().toLocalDate().toString());
        dto.setPlayCount(quiz.getPlayCount());
        dto.setCorrectCount(quiz.getCorrectCount());
        dto.setAccuracyRate(quiz.getAccuracyRate());
        return dto;
    }

    // 정답을 숨기는 버전 (플레이용)
    public static QuizDto fromEntityForPlay(Quiz quiz) {
        QuizDto dto = fromEntity(quiz);
        dto.setAnswer(null); // 정답 숨김
        return dto;
    }
}