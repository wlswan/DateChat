package com.example.dateServer.auth.entity;

import com.example.dateServer.like.Swipe;
import com.example.dateServer.profile.entity.Hobby;
import com.example.dateServer.profile.entity.UserHobby;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @OneToMany(mappedBy = "user" ,cascade = CascadeType.ALL,orphanRemoval = true)
    private Set<UserHobby> userHobbies = new HashSet<>();

    private String bio;

    private String profileImageUrl;

    private LocalDateTime createdAt;


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

    public void updateProfile(String nickname, String bio, String profileImageUrl, Set<Hobby> hobbies) {
        this.nickname = nickname;
        this.bio = bio;
        this.profileImageUrl = profileImageUrl;
        this.userHobbies.clear();
        hobbies.forEach(hobby -> this.userHobbies.add(new UserHobby(this, hobby)));
    }


}
