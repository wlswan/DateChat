package com.example.dateServer.profile.repository;

import com.example.dateServer.profile.entity.UserHobby;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserHobbyRepository extends JpaRepository<UserHobby,Long> {
}
