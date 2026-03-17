package com.example.dateServer.like.dto;

import com.example.dateServer.auth.entity.User;
import com.example.dateServer.common.Lang;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserProfileResponse {
    private Long id;
    private String nickname;
    private Lang lang;
    private String profileImageUrl;

    public static UserProfileResponse from(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .lang(user.getLang())
                .profileImageUrl(user.getProfileImageUrl())
                .build();
    }
}
