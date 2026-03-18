package com.example.dateServer.chat.controller;

import com.example.dateServer.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatService chatService;

    @GetMapping("/rooms")
    public ResponseEntity<?> getChatRooms(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(chatService.getChatRooms(userId));
    }

//    @GetMapping("/{roomId}/messages")
//    public ResponseEntity<?> getMessages(@AuthenticationPrincipal Long userId,
//                                         @PathVariable("roomId") Long roomId) {
//        return ResponseEntity.ok(chatService.getMessagesByRoomId(userId, roomId));
//    }

    @GetMapping("/{roomId}/messages/page")
    public ResponseEntity<?> getMessagesWithCursor(
            @AuthenticationPrincipal Long userId,
            @PathVariable("roomId") Long roomId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cursor,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(chatService.getMessagesByRoomIdWithCursor(userId, roomId, cursor, size));
    }

}
