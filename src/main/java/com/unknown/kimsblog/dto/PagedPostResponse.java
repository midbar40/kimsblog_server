package com.unknown.kimsblog.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class PagedPostResponse {
    private List<PostDto> content;
    private int page;
    private int size;
    private int totalPages;
    private long totalElements;
    private boolean last;

}
