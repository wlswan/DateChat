package com.example.dateServer.chat.repository;

import com.example.dateServer.chat.entity.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage,String> {
    List<ChatMessage> findByRoomIdOrderByCreatedAtAsc(Long roomId);

    List<ChatMessage> findByRoomIdAndReadAtIsNull(Long roomId);

    List<ChatMessage> findByRoomIdAndSenderIdNotAndReadAtIsNull(Long roomId, Long senderId);
}
