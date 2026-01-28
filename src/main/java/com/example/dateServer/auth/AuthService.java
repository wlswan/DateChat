package com.example.dateServer.auth;

import com.example.dateServer.auth.dto.LoginRequest;
import com.example.dateServer.auth.dto.TokenPairs;
import com.example.dateServer.auth.entity.User;
import com.example.dateServer.auth.repository.UserRepository;
import com.example.dateServer.dto.SignupRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    public User signUp(SignupRequest signupRequest) {
        String email = signupRequest.getEmail();
        if(userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException();
        }
        String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());

        User user = User.builder()
                .email(email)
                .password(encodedPassword)
                .lang(signupRequest.getLang())
                .nickname(signupRequest.getNickname())
                .build();

        return userRepository.save(user);

    }

    public TokenPairs login(LoginRequest loginRequest)  {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        User user = userRepository.findUserByEmail(email).orElseThrow(() -> new IllegalArgumentException());

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException();
        }

        String accessToken = jwtProvider.createAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtProvider.createRefreshToken(user.getId());

        return TokenPairs.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

    }

}
