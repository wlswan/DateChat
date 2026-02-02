package com.example.dateServer.profile.repository;

import com.example.dateServer.profile.entity.Hobby;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HobbyRepository extends JpaRepository<Hobby,Long> {
}
