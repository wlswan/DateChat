package com.example.dateServer.dto;

import com.example.dateServer.auth.entity.Lang;
import com.example.dateServer.auth.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignupRequest {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 8,max = 20)
    private String password;

    @NotBlank
    @Size(min = 2, max = 20)
    private String nickname;

    @NotNull
    private Lang lang;

}
