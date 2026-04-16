package com.example.dateServer.chat.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ChatMessagePageResponse {
    private List<ChatMessageResponse> messages;
    private String nextCursor;
    private boolean hasMore; // 무한 스크롤 중단 nextCursor null로도 가능하지만 마지막 페이지에 null이 아닐수도있음
}
