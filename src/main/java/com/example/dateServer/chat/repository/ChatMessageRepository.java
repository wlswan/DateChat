package com.example.dateServer.chat.repository;

import com.example.dateServer.chat.TranslationStatus;
import com.example.dateServer.chat.entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage,String> {
//    List<ChatMessage> findByRoomIdOrderByCreatedAtDesc(Long roomId);

    List<ChatMessage> findByRoomIdOrderByIdDesc(Long roomId, Pageable pageable);

    List<ChatMessage> findByRoomIdAndIdLessThanOrderByIdDesc(
            Long roomId, String cursor, Pageable pageable);


}
