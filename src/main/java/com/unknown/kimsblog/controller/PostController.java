// PostController는 HTTP 요청을 처리하는 컨트롤러 클래스입니다. 예를 들어 게시글 관련 API를 처리할 수 있습니다.

package com.unknown.kimsblog.controller;

import com.unknown.kimsblog.model.Post;
import com.unknown.kimsblog.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin(origins = "http://localhost:5173")  // ✨ 특정 컨트롤러에 CORS 설정
public class PostController {

    @Autowired
    private PostService postService;

    // 게시글 목록 조회
    @GetMapping
    public List<Post> getPosts() {
        return postService.getAllPosts();
    }

    // 게시글 작성
    @PostMapping
    public Post createPost(@RequestBody Post post) {
        return postService.createPost(post);
    }

    // 게시글 조회
    @GetMapping("/{id}")
    public Post getPost(@PathVariable Long id) {
        return postService.getPostById(id);
    }
}
