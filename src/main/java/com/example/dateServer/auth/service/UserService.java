package com.example.dateServer.auth.service;

import com.example.dateServer.auth.dto.ProfileUpdateRequest;
import com.example.dateServer.auth.dto.UserPreferenceRequest;
import com.example.dateServer.auth.dto.UserPreferenceResponse;
import com.example.dateServer.auth.dto.UserResponse;
import com.example.dateServer.auth.entity.User;
import com.example.dateServer.auth.entity.UserPreference;
import com.example.dateServer.auth.exception.UserNotFoundException;
import com.example.dateServer.auth.repository.UserPreferenceRepository;
import com.example.dateServer.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;

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

    public UserPreferenceResponse updatePreference(Long userId, UserPreferenceRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        UserPreference userPreference = UserPreference.builder()
                .user(user)
                .minAge(request.getMinAge())
                .maxAge(request.getMaxAge())
                .minHeight(request.getMinHeight())
                .maxHeight(request.getMaxHeight())
                .build();

        return UserPreferenceResponse.from(userPreferenceRepository.save(userPreference));
    }
}
