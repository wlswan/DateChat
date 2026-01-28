package com.example.dateServer.auth.dto;

import com.example.dateServer.auth.entity.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SignupResponse {

    private Long id;

    private String email;

    private String nickname;

    public static SignupResponse from(User user) {
        return SignupResponse.builder()
                .email(user.getEmail())
                .id(user.getId())
                .nickname(user.getNickname())
                .build();
    }

}
