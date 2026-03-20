package com.example.dateServer.auth;

import com.example.dateServer.auth.entity.Gender;
import lombok.Getter;

import java.time.LocalDate;

@Getter

public class ProfileUpdateRequest {

    private Gender gender;

    private LocalDate birthDate;

    private String bio;

    private String profileImageUrl;

}
