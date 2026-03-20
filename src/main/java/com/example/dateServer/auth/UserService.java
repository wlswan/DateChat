package com.example.dateServer.auth;

import com.example.dateServer.auth.entity.User;
import com.example.dateServer.auth.exception.UserNotFoundException;
import com.example.dateServer.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User getMyInformation(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));
    }

    public UserResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        user.updateProfile(request);
        User save = userRepository.save(user);
        return UserResponse.from(save);
    }
}
