package com.debugify.api.dao;

import com.debugify.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserDao extends JpaRepository<User,Long> {

    boolean existsByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT count(u) FROM User u where u.createdAt >= :oneMonthAgo")
    long countUsersCreatedAfter(@Param("oneMonthAgo") LocalDateTime oneMonthAgo);


}
