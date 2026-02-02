package com.example.dateServer.profile.dto;

import com.example.dateServer.auth.entity.Gender;
import com.example.dateServer.auth.entity.Lang;
import com.example.dateServer.auth.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class ProfileResponse {
    private Long id;
    private String nickname;
    private String bio;
    private String profileImageUrl;
    private Gender gender;
    private LocalDate birthDate;
    private Lang lang;
    private List<HobbyResponse> hobbies;

    public static ProfileResponse from(User user) {
        List<HobbyResponse> hobbyResponses = user.getUserHobbies().stream()
                .map(userHobby -> HobbyResponse.from(userHobby.getHobby()))
                .toList();

        return ProfileResponse.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .bio(user.getBio())
                .profileImageUrl(user.getProfileImageUrl())
                .gender(user.getGender())
                .birthDate(user.getBirthDate())
                .lang(user.getLang())
                .hobbies(hobbyResponses)
                .build();
    }
}
