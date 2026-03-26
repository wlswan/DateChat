package com.example.dateServer.auth.dto;

import com.example.dateServer.auth.entity.UserPreference;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserPreferenceResponse {
    private Long id;
    private Integer minAge;
    private Integer maxAge;
    private Integer minHeight;
    private Integer maxHeight;

    public static UserPreferenceResponse from(UserPreference preference) {
        return UserPreferenceResponse.builder()
                .id(preference.getId())
                .minAge(preference.getMinAge())
                .maxAge(preference.getMaxAge())
                .minHeight(preference.getMinHeight())
                .maxHeight(preference.getMaxHeight())
                .build();
      }                                                                                                                               
  }