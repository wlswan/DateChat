package com.example.dateServer.like;

import com.example.dateServer.auth.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Getter
public class Swipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User fromUser;

    @ManyToOne(fetch = FetchType.LAZY)
    private User toUser;

    @Enumerated(value = EnumType.STRING)
    private SwipeType type;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist(){
        this.createdAt = LocalDateTime.now();
    }

    @Builder
    public Swipe(User fromUser, User toUser, SwipeType type) {
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.type = type;
    }
}
