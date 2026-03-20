package com.example.dateServer.like.dto;


import com.example.dateServer.common.Lang;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MatchResponse {
    private Long matchId;

    private Long partnerId;

    private String partnerNickname;

    private Lang partnerLang;

    private String partnerProfileImageUrl;

    private LocalDateTime matchedAt;

    public MatchResponse(Long matchId, Long partnerId, String partnerNickname, Lang partnerLang, String partnerProfileImageUrl, LocalDateTime matchedAt) {
        this.matchId = matchId;
        this.partnerId = partnerId;
        this.partnerNickname = partnerNickname;
        this.partnerLang = partnerLang;
        this.partnerProfileImageUrl = partnerProfileImageUrl;
        this.matchedAt = matchedAt;
    }
}

