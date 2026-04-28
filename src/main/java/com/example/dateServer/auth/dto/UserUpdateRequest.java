package com.example.dateServer.auth.dto;

import lombok.Getter;

@Getter
public class UserUpdateRequest {
    private String bio;
    private String profileImageUrl;
    private Integer minAge;
    private Integer maxAge;
    private Integer minHeight;
    private Integer maxHeight;
}
