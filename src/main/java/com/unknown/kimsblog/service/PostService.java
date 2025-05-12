//PostService는 비즈니스 로직을 담당합니다. 데이터를 가져오거나 저장하는 등의 작업을 합니다.

package com.unknown.kimsblog.service;

import com.unknown.kimsblog.model.Post;
import com.unknown.kimsblog.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public Post createPost(Post post) {
        return postRepository.save(post);
    }

    public Post getPostById(Long id) {
        Optional<Post> post = postRepository.findById(id);
        return post.orElseThrow(() -> new RuntimeException("Post not found"));
    }
}
