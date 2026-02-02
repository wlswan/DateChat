package com.example.dateServer.profile.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProfileUpdateRequest {
    private String nickname;

    private String bio;

    private String profileImageUrl;

    private List<Long> hobbyIds;
}
