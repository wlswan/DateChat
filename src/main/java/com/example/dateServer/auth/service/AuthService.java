package com.example.dateServer.auth.service;

import com.example.dateServer.auth.dto.LoginRequest;
import com.example.dateServer.auth.dto.TokenPairs;
import com.example.dateServer.auth.entity.User;
import com.example.dateServer.auth.exception.DuplicateEmailException;
import com.example.dateServer.auth.exception.InvalidPasswordException;
import com.example.dateServer.auth.exception.InvalidRefreshTokenException;
import com.example.dateServer.auth.exception.UserNotFoundException;
import com.example.dateServer.auth.repository.UserRepository;
import com.example.dateServer.auth.dto.SignupRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService tokenBlacklistService;

    public User signUp(SignupRequest signupRequest) {
        String email = signupRequest.getEmail();
        if(userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException(email);
        }
        String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());

        User user = User.builder()
                .email(email)
                .password(encodedPassword)
                .lang(signupRequest.getLang())
                .nickname(signupRequest.getNickname())
                .gender(signupRequest.getGender())
                .birthDate(signupRequest.getBirthDate())
                .build();

        return userRepository.save(user);

    }

    public TokenPairs login(LoginRequest loginRequest)  {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidPasswordException();
        }

        String accessToken = jwtProvider.createAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtProvider.createRefreshToken(user.getId());

        refreshTokenService.save(user.getId(), refreshToken);

        return TokenPairs.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

    }

    public TokenPairs refresh(String refreshToken) {
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new InvalidRefreshTokenException();
        }

        Long userId = jwtProvider.getUserId(refreshToken);

        if (!refreshTokenService.exists(refreshToken, userId)) {
            throw new InvalidRefreshTokenException();
        }

        refreshTokenService.delete(refreshToken, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        String newAccessToken = jwtProvider.createAccessToken(user.getId(), user.getEmail());
        String newRefreshToken = jwtProvider.createRefreshToken(user.getId());

        refreshTokenService.save(userId, newRefreshToken);

        return TokenPairs.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    public void logout(String accessToken, String refreshToken) {
        if (refreshToken != null && jwtProvider.validateToken(refreshToken)) {
            Long userId = jwtProvider.getUserId(refreshToken);
            refreshTokenService.delete(refreshToken, userId);
        }

        if (accessToken != null && jwtProvider.validateToken(accessToken)) {
            long ttl = jwtProvider.getRemainingTtl(accessToken);
            tokenBlacklistService.add(accessToken, ttl);
        }
    }
}
