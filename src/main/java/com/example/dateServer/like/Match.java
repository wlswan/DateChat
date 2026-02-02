package com.example.dateServer.like;

import com.example.dateServer.auth.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    private User user1;

    @ManyToOne(fetch = FetchType.LAZY ,optional = false)
    private User user2;

    @Column(nullable = true)
    private Long chatRoomId;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @Builder
    public Match(User user1, User user2, Long chatRoomId) {
        this.user1 = user1;
        this.user2 = user2;
        this.chatRoomId = chatRoomId;
    }
}
