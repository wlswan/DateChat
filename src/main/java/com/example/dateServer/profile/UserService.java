package com.example.dateServer.profile;

import com.example.dateServer.auth.entity.User;
import com.example.dateServer.auth.repository.UserRepository;
import com.example.dateServer.profile.dto.ProfileResponse;
import com.example.dateServer.profile.dto.ProfileUpdateRequest;
import com.example.dateServer.profile.entity.Hobby;
import com.example.dateServer.profile.repository.HobbyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final HobbyRepository hobbyRepository;

    public ProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(IllegalArgumentException::new);
        return ProfileResponse.from(user);
    }

    @Transactional
    public ProfileResponse updateProfile(Long userId, ProfileUpdateRequest request){
        User user = userRepository.findById(userId).orElseThrow(IllegalArgumentException::new);
       Set<Hobby> hobbies= new HashSet<>(hobbyRepository.findAllById(request.getHobbyIds()));
       user.updateProfile(request.getNickname(), request.getBio(),request.getProfileImageUrl(),hobbies);

        return ProfileResponse.from(user);

    }

}
