// QuizSearchRequest.java
package com.unknown.kimsblog.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuizSearchRequest {
    private String category;
    private String difficulty;
    private String keyword;
    private String sortBy = "createdAt"; // createdAt, playCount, accuracyRate
    private String sortDirection = "desc"; // asc, desc
    private Integer page = 0;
    private Integer size = 20;
}