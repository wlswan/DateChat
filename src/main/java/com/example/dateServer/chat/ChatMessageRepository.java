package com.example.dateServer.chat;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage,String> {
    List<ChatMessage> findByRoomIdOrderByCreatedAtAsc(Long roomId);

    List<ChatMessage> findByRoomIdAndReadAtIsNull(Long roomId);

    List<ChatMessage> findByRoomIdAndSenderIdNotAndReadAtIsNull(Long roomId, Long senderId);
}
