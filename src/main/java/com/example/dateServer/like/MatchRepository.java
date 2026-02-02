package com.example.dateServer.like;

import com.example.dateServer.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {
    public List<Match> findByUser1OrUser2(User user1, User user2);
}
