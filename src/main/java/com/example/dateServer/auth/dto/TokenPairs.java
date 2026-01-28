package com.example.dateServer.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenPairs {
    private String accessToken;
    private String refreshToken;


}
