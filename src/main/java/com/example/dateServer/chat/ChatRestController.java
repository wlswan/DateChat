package com.example.dateServer.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatService chatService;

    @GetMapping("/{roomId}/messages")
    public ResponseEntity<?> getMessages(@AuthenticationPrincipal Long userId,
                                         @PathVariable("roomId") Long roomId) {
        return ResponseEntity.ok(chatService.getMessagesByRoomId(userId,roomId));

    }

}
