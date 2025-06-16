// QuizResultDto.java
package com.unknown.kimsblog.dto;

import com.unknown.kimsblog.model.QuizResult;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuizResultDto {
    private Long id;
    private Long quizId;
    private String question;
    private String userAnswer;
    private String correctAnswer;
    private Boolean isCorrect;
    private Integer timeTaken;
    private String answeredAt;
    private String category;
    private String difficulty;

    public static QuizResultDto fromEntity(QuizResult result) {
        QuizResultDto dto = new QuizResultDto();
        dto.setId(result.getId());
        dto.setQuizId(result.getQuiz().getId());
        dto.setQuestion(result.getQuiz().getQuestion());
        dto.setUserAnswer(result.getUserAnswer());
        dto.setCorrectAnswer(result.getQuiz().getAnswer());
        dto.setIsCorrect(result.getIsCorrect());
        dto.setTimeTaken(result.getTimeTaken());
        dto.setAnsweredAt(result.getAnsweredAt().toString());
        dto.setCategory(result.getQuiz().getCategory());
        dto.setDifficulty(result.getQuiz().getDifficulty().name().toLowerCase());
        return dto;
    }
}