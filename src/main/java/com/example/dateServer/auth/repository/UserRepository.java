package com.example.dateServer.auth.repository;

import com.example.dateServer.auth.entity.Gender;
import com.example.dateServer.auth.entity.User;
import com.example.dateServer.common.Lang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findUserByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.id != :userId " +
            "AND u.gender != :gender " +
            "AND u.lang != :lang " +
            "AND YEAR(CURRENT_DATE) - YEAR(u.birthDate) BETWEEN :minAge AND :maxAge " +
            "AND NOT EXISTS (SELECT 1 FROM Swipe s WHERE s.fromUser.id = :userId AND s.toUser.id = u.id)")
    List<User> findDiscoverCandidates(@Param("userId") Long userId,
                                      @Param("gender")Gender gender,
                                      @Param("lang")Lang lang,
                                      @Param("minAge")int minAge,
                                      @Param("maxAge")int maxAge);
}
