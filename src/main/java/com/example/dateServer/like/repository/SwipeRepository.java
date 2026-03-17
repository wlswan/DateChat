package com.example.dateServer.like.repository;

import com.example.dateServer.auth.entity.User;
import com.example.dateServer.like.entity.Swipe;
import com.example.dateServer.like.entity.SwipeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SwipeRepository extends JpaRepository<Swipe,Long> {
    boolean existsByFromUserAndToUser(User fromUser, User toUser);

    boolean existsByFromUserAndToUserAndType(User fromUser, User toUser, SwipeType type);

    @Query("SELECT s.toUser.id FROM Swipe s WHERE s.fromUser.id = :userId")
    List<Long> findSwipedUserIdsByMe(@Param("userId") Long userId);
}
