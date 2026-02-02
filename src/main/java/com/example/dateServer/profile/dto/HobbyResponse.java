package com.example.dateServer.profile.dto;

import com.example.dateServer.profile.entity.Hobby;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HobbyResponse {
    private Long id;
    private String nameKo;
    private String nameJa;

    public static HobbyResponse from(Hobby hobby) {
        return HobbyResponse.builder()
                .id(hobby.getId())
                .nameKo(hobby.getNameKo())
                .nameJa(hobby.getNameJa())
                .build();
    }
}
