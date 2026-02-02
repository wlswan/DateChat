package com.example.dateServer.like;

import com.example.dateServer.auth.entity.User;
import lombok.Data;

@Data
public class SwipeRequest {
    private Long toUserId;
    private SwipeType type;

}
