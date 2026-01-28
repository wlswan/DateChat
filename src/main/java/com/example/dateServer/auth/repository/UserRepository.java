package com.example.dateServer.auth.repository;

import com.example.dateServer.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findUserByEmail(String email);

    boolean existsByEmail(String email);

}
