package com.unknown.kimsblog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.unknown.kimsblog.model.TemporaryPost;
import java.util.Optional;

public interface TemporaryPostRepository extends JpaRepository<TemporaryPost, Long>{
    Optional<TemporaryPost> findFirstByOrderByCreatedAtDesc();
}
