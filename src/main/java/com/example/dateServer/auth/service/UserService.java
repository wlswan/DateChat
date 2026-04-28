package com.example.dateServer.auth.service;

import com.example.dateServer.auth.dto.UserUpdateRequest;
import com.example.dateServer.auth.dto.UserResponse;
import com.example.dateServer.auth.entity.User;
import com.example.dateServer.auth.exception.UserNotFoundException;
import com.example.dateServer.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public UserResponse getMe(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return UserResponse.from(user, user.getUserPreference());
    }

    @Transactional
    public UserResponse updateMe(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        user.updateProfile(request);
        user.updatePreference(request.getMinAge(), request.getMaxAge(), request.getMinHeight(), request.getMaxHeight());
        userRepository.save(user);
        return UserResponse.from(user, user.getUserPreference());
    }
}
