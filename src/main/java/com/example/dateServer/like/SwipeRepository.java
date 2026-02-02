package com.example.dateServer.like;

import com.example.dateServer.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SwipeRepository extends JpaRepository<Swipe,Long> {
    boolean existsByFromUserAndToUser(User fromUser, User toUser);

    boolean existsByFromUserAndToUserAndType(User fromUser, User toUser,SwipeType type);
}
