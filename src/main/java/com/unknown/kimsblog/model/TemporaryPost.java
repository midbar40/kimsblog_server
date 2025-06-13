package com.unknown.kimsblog.model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name= "temporary_posts") // 임시 저장 테이블

public class TemporaryPost {
    @Id
    private Long id = 1L; // id=1로 고정, 자동 증가 X
    private String title;
    private String content;

    @Column(name="created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(name="updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public TemporaryPost(){
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void updatePost(String title, String content) {
        this.title = title;
        this.content = content;
        this.updatedAt = LocalDateTime.now(); // ✨ 수정 시 업데이트
    }

}
