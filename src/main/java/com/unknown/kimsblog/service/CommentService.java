package com.unknown.kimsblog.service;

import com.unknown.kimsblog.dto.CommentCreateDto;
import com.unknown.kimsblog.dto.CommentDeleteDto;
import com.unknown.kimsblog.dto.CommentResponseDto;
import com.unknown.kimsblog.dto.CommentUpdateDto;
import com.unknown.kimsblog.model.Comment;
import com.unknown.kimsblog.model.Post;
import com.unknown.kimsblog.repository.CommentRepository;
import com.unknown.kimsblog.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final PasswordEncoder passwordEncoder;

    // 댓글 생성
    public CommentResponseDto createComment(Long postId, CommentCreateDto commentCreateDto) {
        // 포스트 존재 확인
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("포스트를 찾을 수 없습니다. ID: " + postId));

        // 댓글 생성
        Comment comment = Comment.builder()
                .nickname(commentCreateDto.getNickname())
                .password(passwordEncoder.encode(commentCreateDto.getPassword())) // 비밀번호 암호화
                .profileImage(commentCreateDto.getProfileImage())
                .content(commentCreateDto.getContent())
                .post(post)
                .build();

        Comment savedComment = commentRepository.save(comment);
        log.info("댓글 생성 완료. ID: {}, 포스트 ID: {}", savedComment.getId(), postId);

        return convertToResponseDto(savedComment);
    }

    // 특정 포스트의 댓글 목록 조회
    @Transactional(readOnly = true)
    public List<CommentResponseDto> getCommentsByPostId(Long postId) {
        // 포스트 존재 확인
        if (!postRepository.existsById(postId)) {
            throw new IllegalArgumentException("포스트를 찾을 수 없습니다. ID: " + postId);
        }

        List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtAsc(postId);
        return comments.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    // 댓글 수정
    public CommentResponseDto updateComment(Long commentId, CommentUpdateDto commentUpdateDto) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다. ID: " + commentId));

        // 비밀번호 확인
        if (!passwordEncoder.matches(commentUpdateDto.getPassword(), comment.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 댓글 내용 수정
        comment.setContent(commentUpdateDto.getContent());
        Comment updatedComment = commentRepository.save(comment);

        log.info("댓글 수정 완료. ID: {}", commentId);

        return convertToResponseDto(updatedComment);
    }

    // 댓글 삭제
    public void deleteComment(Long commentId, CommentDeleteDto commentDeleteDto) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다. ID: " + commentId));

        // 비밀번호 확인
        if (!passwordEncoder.matches(commentDeleteDto.getPassword(), comment.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        commentRepository.delete(comment);
        log.info("댓글 삭제 완료. ID: {}", commentId);
    }

    // 특정 포스트의 댓글 수 조회
    @Transactional(readOnly = true)
    public Long getCommentCountByPostId(Long postId) {
        return commentRepository.countByPostId(postId);
    }

    // Entity -> ResponseDto 변환
    private CommentResponseDto convertToResponseDto(Comment comment) {
        return CommentResponseDto.builder()
                .id(comment.getId())
                .nickname(comment.getNickname())
                .profileImage(comment.getProfileImage())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}