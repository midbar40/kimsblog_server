package com.unknown.kimsblog.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

// 댓글 삭제 요청 DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDeleteDto {

    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;
}
