package com.unknown.kimsblog.service;

import org.springframework.stereotype.Service;
import com.unknown.kimsblog.model.TemporaryPost;
import com.unknown.kimsblog.repository.TemporaryPostRepository;

import java.util.List;
import java.util.Optional;

@Service
public class TemporaryPostService {
    private final TemporaryPostRepository temporaryPostRepository;

    public TemporaryPostService(TemporaryPostRepository temporaryPostRepository){
        this.temporaryPostRepository = temporaryPostRepository;
    }


    public TemporaryPost saveTemporaryPost(TemporaryPost post) {
        temporaryPostRepository.deleteAll(); // ✅ 기존 임시 저장 글 전체 삭제 (항상 하나만 유지)
        return temporaryPostRepository.save(post); // ✅ 새로운 글 저장
    }

    public Optional<TemporaryPost> getLatestTemporaryPost() {
        return temporaryPostRepository.findFirstByOrderByCreatedAtDesc();
    }

    public void deleteAllTemporaryPosts() {
        temporaryPostRepository.deleteAll();
    }

}
