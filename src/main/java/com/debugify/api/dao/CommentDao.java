package com.debugify.api.dao;

import com.debugify.api.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CommentDao extends JpaRepository<Comment,Long> {
    @Query("SELECT c FROM Comment c WHERE c.postId = :postId ORDER BY c.createdAt DESC")
    List<Comment> findByPostIdOrderByCreatedAtDesc(@Param("postId") Long postId);

    @Query("SELECT c FROM Comment c ORDER BY c.createdAt DESC")
    Page<Comment> findAllOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT c FROM Comment c ORDER BY c.createdAt ASC")
    Page<Comment> findAllOrderByCreatedAtAsc(Pageable pageable);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.createdAt >= :startDate")
    long countCommentsCreatedAfter(@Param("startDate") LocalDateTime startDate);

    void deleteByPostId(Long postId);
}
