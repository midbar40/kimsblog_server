// PostRepository는 데이터베이스와의 상호작용을 담당합니다. Spring Data JPA를 사용하여 CRUD 작업을 처리합니다.

package com.unknown.kimsblog.repository;

import com.unknown.kimsblog.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
