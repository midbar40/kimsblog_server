// CreateQuizRequest.java
package com.unknown.kimsblog.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateQuizRequest {

    @NotBlank(message = "문제는 필수입니다")
    @Size(max = 1000, message = "문제는 1000자를 초과할 수 없습니다")
    private String question;

    @NotBlank(message = "정답은 필수입니다")
    @Size(max = 500, message = "정답은 500자를 초과할 수 없습니다")
    private String answer;

    @NotNull(message = "제한 시간은 필수입니다")
    @Min(value = 5, message = "제한 시간은 최소 5초입니다")
    @Max(value = 300, message = "제한 시간은 최대 300초입니다")
    private Integer timeLimit;

    @NotBlank(message = "카테고리는 필수입니다")
    @Size(max = 100, message = "카테고리는 100자를 초과할 수 없습니다")
    private String category;

    @NotBlank(message = "난이도는 필수입니다")
    @Pattern(regexp = "easy|medium|hard", message = "난이도는 easy, medium, hard 중 하나여야 합니다")
    private String difficulty;
}