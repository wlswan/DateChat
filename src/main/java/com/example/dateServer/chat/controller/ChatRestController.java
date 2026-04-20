package com.example.dateServer.chat.controller;

import com.example.dateServer.chat.ChatEventType;
import com.example.dateServer.chat.dto.ChatEventBroadcast;
import com.example.dateServer.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatService chatService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @GetMapping("/rooms")
    public ResponseEntity<?> getChatRooms(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(chatService.getChatRooms(userId));
    }

//    @GetMapping("/{roomId}/messages")
//    public ResponseEntity<?> getMessages(@AuthenticationPrincipal Long userId,
//                                         @PathVariable("roomId") Long roomId) {
//        return ResponseEntity.ok(chatService.getMessagesByRoomId(userId, roomId));
//    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<?> leaveRoom(@AuthenticationPrincipal Long userId,
                                       @PathVariable("roomId") Long roomId) {
        chatService.leaveRoom(userId, roomId);
        simpMessagingTemplate.convertAndSend(
                "/topic/chat/" + roomId + "/events",
                new ChatEventBroadcast(ChatEventType.ROOM_CLOSED, roomId, userId));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{roomId}/messages/page")
    public ResponseEntity<?> getMessagesWithCursor(
            @AuthenticationPrincipal Long userId,
            @PathVariable("roomId") Long roomId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(chatService.getMessagesByRoomIdWithCursor(userId, roomId, cursor, size));
    }

}
