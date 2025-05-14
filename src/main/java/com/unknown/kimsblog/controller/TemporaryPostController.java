package com.unknown.kimsblog.controller;

import org.springframework.web.bind.annotation.*;
import com.unknown.kimsblog.model.TemporaryPost;
import com.unknown.kimsblog.service.TemporaryPostService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/temp-posts")
@CrossOrigin(origins = "http://localhost:5173")  // ✨ 특정 컨트롤러에 CORS 설정

public class TemporaryPostController {
    private final TemporaryPostService temporaryPostService;

    public TemporaryPostController(TemporaryPostService temporaryPostService) {
        this.temporaryPostService = temporaryPostService;
    }

    // ✅ 새로운 글 저장 (기존 데이터 삭제 후 저장)
    @PostMapping
    public TemporaryPost createTemporaryPost(@RequestBody TemporaryPost post) {
        return temporaryPostService.saveTemporaryPost(post); // ✅ 기존 글 삭제 후 저장됨
    }

    // ✅ 기존 임시 저장된 글 조회 (항상 하나만 반환)
    @GetMapping
    public Optional<TemporaryPost> getLatestTemporaryPost() {
        return temporaryPostService.getLatestTemporaryPost(); // ✅ 최신 글 반환
    }

    // ✅ 임시 저장 글 삭제
    @DeleteMapping
    public void deleteTemporaryPost() {
        temporaryPostService.deleteAllTemporaryPosts(); // ✅ 전체 삭제가 아니라 "현재 하나만 유지"하는 방식으로 유지
    }


}
