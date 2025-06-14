//PostService는 비즈니스 로직을 담당합니다. 데이터를 가져오거나 저장하는 등의 작업을 합니다.

package com.unknown.kimsblog.service;

import com.unknown.kimsblog.model.Post;
import com.unknown.kimsblog.repository.PostRepository;
import com.unknown.kimsblog.repository.TemporaryPostRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;

import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor

public class PostService {

    private final PostRepository postRepository;
    private final TemporaryPostRepository temporaryPostRepository;

    public List<Post> getAllPosts() {
        return postRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public Page<Post> getAllPostsPaged(Pageable pageable){
        return postRepository.findAll(pageable);
    }

    @Transactional
    public Post createPost(Post post) {
        return postRepository.save(post);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteAllTemporaryPosts() {
        temporaryPostRepository.deleteAll(); // id=1 삭제
    }

    public Post updatePost(Long id, Post updatedPost) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        post.updatePost(updatedPost.getTitle(), updatedPost.getContent()); // ✨ 기존 메서드 활용
        post.setUpdatedAt(java.time.LocalDateTime.now()); // ✨ 수정 시간 갱신

        return postRepository.save(post);
    }

    public Post getPostById(Long id) {
        Optional<Post> post = postRepository.findById(id);
        return post.orElseThrow(() -> new RuntimeException("Post not found"));
    }

    public void deletePostById(Long id) {
        postRepository.deleteById(id);
    }
}
