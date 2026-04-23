package com.example.dateServer.auth.entity;

import com.example.dateServer.auth.dto.ProfileUpdateRequest;
import com.example.dateServer.common.Lang;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    private String password;

    private String nickname;

    @Enumerated(EnumType.STRING)
    private Lang lang;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private LocalDate birthDate;

    private String bio;

    private String profileImageUrl;

    private LocalDateTime createdAt;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserPreference userPreference;


    @PrePersist
    public void prePersist(){
        this.createdAt = LocalDateTime.now();
    }

    @Builder
    public User(String email, String password, String nickname, Lang lang, Gender gender, LocalDate birthDate) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.lang = lang;
        this.gender = gender;
        this.birthDate = birthDate;
    }

    public void updateProfile(ProfileUpdateRequest request) {
        this.gender = request.getGender();
        this.birthDate = request.getBirthDate();
        this.bio = request.getBio();
        this.profileImageUrl = request.getProfileImageUrl();
    }

    public void createPreference(Integer minAge, Integer maxAge, Integer minHeight, Integer maxHeight) {
        this.userPreference = UserPreference.builder()
                .user(this)
                .minAge(minAge)
                .maxAge(maxAge)
                .minHeight(minHeight)
                .maxHeight(maxHeight)
                .build();
    }
}
