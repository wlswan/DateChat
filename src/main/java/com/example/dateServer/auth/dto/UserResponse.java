package com.example.dateServer.auth.dto;

import com.example.dateServer.auth.entity.Gender;
import com.example.dateServer.auth.entity.User;
import com.example.dateServer.common.Lang;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String email;
    private String nickname;
    private Lang appLang;
    private Gender gender;
    private LocalDate birthDate;
    private String bio;
    private String profileImageUrl;
    private LocalDateTime createdAt;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .appLang(user.getLang())
                .gender(user.getGender())
                .birthDate(user.getBirthDate())
                .bio(user.getBio())
                .profileImageUrl(user.getProfileImageUrl())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
