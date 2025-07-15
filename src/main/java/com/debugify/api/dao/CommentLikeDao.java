package com.debugify.api.dao;

import com.debugify.api.entity.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentLikeDao extends JpaRepository<CommentLike,Long> {
    @Query("SELECT cl FROM CommentLike cl WHERE cl.comment.id = :commentId AND cl.user.id = :userId")
    Optional<CommentLike> findByCommentIdAndUserId(@Param("commentId") Long commentId, @Param("userId") Long userId);

    @Query("SELECT cl FROM CommentLike cl WHERE cl.comment.id = :commentId")
    List<CommentLike> findByCommentId(@Param("commentId") Long commentId);

    @Query("SELECT cl.user.id FROM CommentLike cl WHERE cl.comment.id = :commentId")
    List<Long> findUserIdsByCommentId(@Param("commentId") Long commentId);

    boolean existsByCommentIdAndUserId(Long commentId, Long userId);

    void deleteByCommentId(Long commentId);
}
