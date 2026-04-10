package com.example.dateServer.chat.repository;

import com.example.dateServer.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query("SELECT cr FROM ChatRoom cr JOIN FETCH cr.match m JOIN FETCH m.user1 JOIN FETCH m.user2 WHERE (m.user1.id = :userId OR m.user2.id = :userId) AND cr.status = 'ACTIVE'")
    List<ChatRoom> findRoomsWithMatchAndUsersByUserId(@Param("userId") Long userId);

    @Query("SELECT cr FROM ChatRoom cr JOIN FETCH cr.match m JOIN FETCH m.user1 JOIN FETCH m.user2 WHERE cr.id = :roomId")
    Optional<ChatRoom> findByIdWithUsers(@Param("roomId") Long roomId);

    Optional<ChatRoom> findByMatchId(Long matchId);

    @Query("SELECT cr FROM ChatRoom cr JOIN cr.match m WHERE (m.user1.id = :userId1 AND m.user2.id = :userId2) OR (m.user1.id = :userId2 AND m.user2.id = :userId1)")
    Optional<ChatRoom> findByUserIds(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
}
