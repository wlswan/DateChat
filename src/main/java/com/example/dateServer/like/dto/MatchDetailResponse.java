package com.example.dateServer.like.dto;

import com.example.dateServer.auth.entity.Gender;
import com.example.dateServer.auth.entity.User;
import com.example.dateServer.common.Lang;
import com.example.dateServer.like.entity.Match;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class MatchDetailResponse {

    private Long matchId;

    private Long partnerId;

    private String partnerNickname;

    private Lang partnerLang;

    private Gender partnerGender;

    private LocalDate partnerBirthDate;

    private String partnerBio;

    private String partnerProfileImageUrl;

    private LocalDateTime matchedAt;

    public static MatchDetailResponse from(User partnerUser, Match match) {
        return MatchDetailResponse.builder()
                .matchId(match.getId())
                .partnerId(partnerUser.getId())
                .partnerNickname(partnerUser.getNickname())
                .partnerLang(partnerUser.getLang())
                .partnerGender(partnerUser.getGender())
                .partnerBirthDate(partnerUser.getBirthDate())
                .partnerBio(partnerUser.getBio())
                .partnerProfileImageUrl(partnerUser.getProfileImageUrl())
                .matchedAt(match.getCreatedAt())
                .build();

    }
}
