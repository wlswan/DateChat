package com.example.dateServer.like.repository;

import com.example.dateServer.like.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MatchRepository extends JpaRepository<Match, Long> {
    @Query("SELECT m FROM Match m JOIN FETCH m.user1 JOIN FETCH m.user2 WHERE m.user1.id = :userId OR m.user2.id = :userId")
    List<Match> findMatchesWithUsersByUserId(@Param("userId") Long userId);

    @Query("SELECT m FROM Match m JOIN FETCH m.user1 JOIN FETCH m.user2 WHERE m.id = :id")
    Optional<Match> findByIdWithUsers(@Param("id") Long id);
}
