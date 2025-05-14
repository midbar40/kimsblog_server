// Post는 게시글 데이터를 저장할 엔티티 클래스입니다. JPA를 사용하여 데이터베이스와 매핑됩니다.

package com.unknown.kimsblog.model;

import jakarta.persistence.*;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "posts") // ✨ 테이블명을 명확하게 지정
public class Post {

    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Setter
    private String title;
    @Setter
    private String content;

    @Column(name = "created_at", insertable = false, updatable = false, nullable = false) // ✨ DB에 있는 `created_at` 컬럼과 매핑
    private java.time.LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false, nullable = false) // ✨ DB에 있는 `created_at` 컬럼과 매핑
    @Setter
    private LocalDateTime updatedAt;

    // 기본 생성자, 게터, 세터
    public Post() {
    }

    public Post(String title, String content) {
        this.title = title;
        this.content = content;
        this.createdAt = java.time.LocalDateTime.now(); // ✨ 생성 시 `created_at` 설정
        this.updatedAt = java.time.LocalDateTime.now(); // 업데이트시 updated_at 설정
    }

    public void updatePost(String title, String content) {
        this.title = title;
        this.content = content;
        this.updatedAt = java.time.LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }

}
