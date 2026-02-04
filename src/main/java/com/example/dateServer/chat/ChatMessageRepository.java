package com.example.dateServer.chat;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage,String> {
    public List<ChatMessage> findByRoomIdOrderByCreatedAtAsc(Long roomId);

    public List<ChatMessage> findByRoomIdAndReadAtIsNull(Long roomId);
}
