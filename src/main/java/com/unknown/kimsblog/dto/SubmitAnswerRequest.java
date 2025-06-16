// SubmitAnswerRequest.java
package com.unknown.kimsblog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubmitAnswerRequest {

    @NotNull(message = "퀴즈 ID는 필수입니다")
    private Long quizId;

    @NotBlank(message = "답안은 필수입니다")
    private String userAnswer;

    @NotNull(message = "소요 시간은 필수입니다")
    @Min(value = 0, message = "소요 시간은 0 이상이어야 합니다")
    @Max(value = 300, message = "소요 시간은 300초를 초과할 수 없습니다")
    private Integer timeTaken;
}