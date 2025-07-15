package com.debugify.api.dao;

import com.debugify.api.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PostDao extends JpaRepository<Post,Long> {
    Optional<Post> findBySlug(String slug);

    boolean existsByTitle(String title);

    boolean existsBySlug(String slug);

    Page<Post> findByUserId(Long userId, Pageable pageable);

    Page<Post> findByCategory(String category, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.userId = :userId AND p.category = :category")
    Page<Post> findByUserIdAndCategory(@Param("userId") Long userId, @Param("category") String category, Pageable pageable);

    @Query(value = "SELECT * FROM posts p WHERE " +
            "(:searchTerm IS NULL OR " +
            "CAST(p.title AS TEXT) LIKE CONCAT('%', CAST(:searchTerm AS TEXT), '%') OR " +
            "CAST(p.content AS TEXT) LIKE CONCAT('%', CAST(:searchTerm AS TEXT), '%')) AND " +
            "(:category IS NULL OR CAST(p.category AS TEXT) = CAST(:category AS TEXT))",
            countQuery = "SELECT COUNT(*) FROM posts p WHERE " +
            "(:searchTerm IS NULL OR " +
            "CAST(p.title AS TEXT) LIKE CONCAT('%', CAST(:searchTerm AS TEXT), '%') OR " +
            "CAST(p.content AS TEXT) LIKE CONCAT('%', CAST(:searchTerm AS TEXT), '%')) AND " +
            "(:category IS NULL OR CAST(p.category AS TEXT) = CAST(:category AS TEXT))",
            nativeQuery = true)
    Page<Post> findWithFilters(@Param("searchTerm") String searchTerm,
                               @Param("category") String category,
                               Pageable pageable);

    @Query("SELECT COUNT(p) FROM Post p WHERE p.createdAt >= :startDate")
    long countPostsCreatedAfter(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT p FROM Post p WHERE p.userId = :userId")
    Optional<Post> findByUserId(@Param("userId") Long userId);
}
