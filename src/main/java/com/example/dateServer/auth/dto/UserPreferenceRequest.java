package com.example.dateServer.auth.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserPreferenceRequest {
    @NotNull
    private Integer minAge;

    @NotNull
    private Integer maxAge;

    @NotNull
    private Integer minHeight;

    @NotNull
    private Integer maxHeight;
}
