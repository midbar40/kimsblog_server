// PostController HTTP 요청을 처리하는 컨트롤러 클래스입니다. 예를 들어 게시글 관련 API를 처리할 수 있습니다.

package com.unknown.kimsblog.controller;

import com.unknown.kimsblog.model.Post;
import com.unknown.kimsblog.service.PostService;
import org.hibernate.sql.Delete;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.unknown.kimsblog.service.TemporaryPostService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin(origins = "http://localhost:5173")  // ✨ 특정 컨트롤러에 CORS 설정
public class PostController {

    private final PostService postService;
    private final TemporaryPostService temporaryPostService;

    public PostController(PostService postService,TemporaryPostService temporaryPostService){
        this.postService = postService;
        this.temporaryPostService = temporaryPostService;
    }

    // 게시글 목록 조회
    @GetMapping
    public List<Post> getPosts() {
        return postService.getAllPosts();
    }

    // 게시글 작성
    @PostMapping
    public Post createPost(@RequestBody Post post) {
        Post savedPost = postService.createPost(post); // ✨ 일반 글 저장
        temporaryPostService.deleteAllTemporaryPosts();// ✅ 임시 저장 삭제
        return savedPost;
    }

    // 게시글 조회
    @GetMapping("/{id}")
    public Post getPost(@PathVariable Long id) {
        return postService.getPostById(id);
    }

    // 게시글 수정
    @PutMapping("/{id}")
    public Post updatePost(@PathVariable Long id, @RequestBody Post post) {
        return postService.updatePost(id, post); // ✅ 수정된 게시글 반환
    }

    // 게시글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePostById(id);
        return ResponseEntity.noContent()
                .build();

    }
}
