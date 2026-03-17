package com.example.dateServer.like.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SwipeResult {
    private boolean matched;
    private Long roomId;
}
