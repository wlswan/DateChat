package com.example.dateServer.chat.repository;

import com.example.dateServer.chat.TranslationStatus;
import com.example.dateServer.chat.entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage,String> {
//    List<ChatMessage> findByRoomIdOrderByCreatedAtDesc(Long roomId);

    List<ChatMessage> findByRoomIdOrderByIdDesc(Long roomId, Pageable pageable);

    // 커서 이전 메시지 (위로 스크롤) — ObjectId 자체가 시간 정보를 포함하므로 동시성 누락 없음
    List<ChatMessage> findByRoomIdAndIdLessThanOrderByIdDesc(
            Long roomId, String cursor, Pageable pageable);

    // 타임아웃 처리용: 특정 시간 이전에 생성된 PENDING 메시지 조회
    List<ChatMessage> findByTranslationStatusAndCreatedAtBefore(
            TranslationStatus translationStatus, LocalDateTime threshold);
}
