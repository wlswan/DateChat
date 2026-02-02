package com.example.dateServer.profile.entity;

import com.example.dateServer.auth.entity.User;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class UserHobby {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hobby_id")
    private Hobby hobby;


    public UserHobby(User user, Hobby hobby) {
        this.user = user;
        this.hobby = hobby;
    }
}
