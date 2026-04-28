package com.example.dateServer.auth.dto;

import com.example.dateServer.auth.entity.Gender;
import com.example.dateServer.auth.entity.User;
import com.example.dateServer.auth.entity.UserPreference;
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
    private Integer minAge;
    private Integer maxAge;
    private Integer minHeight;
    private Integer maxHeight;

    public static UserResponse from(User user, UserPreference preference) {
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
                .minAge(preference != null ? preference.getMinAge() : null)
                .maxAge(preference != null ? preference.getMaxAge() : null)
                .minHeight(preference != null ? preference.getMinHeight() : null)
                .maxHeight(preference != null ? preference.getMaxHeight() : null)
                .build();
    }
}
