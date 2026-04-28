package com.example.dateServer.auth.dto;

import com.example.dateServer.auth.entity.Gender;
import com.example.dateServer.common.Lang;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SignupRequest {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 8, max = 20)
    private String password;

    @NotBlank
    @Size(min = 2, max = 20)
    private String nickname;

    @NotNull
    private Lang lang;

    @NotNull
    private Gender gender;

    @NotNull
    private LocalDate birthDate;
}
