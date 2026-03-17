package com.example.dateServer.like.dto;

import com.example.dateServer.like.entity.SwipeType;
import lombok.Data;

@Data
public class SwipeRequest {
    private Long toUserId;
    private SwipeType type;

}
