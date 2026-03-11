package com.example.dateServer.auth;

import com.example.dateServer.auth.dto.LoginRequest;
import com.example.dateServer.auth.dto.LoginResponse;
import com.example.dateServer.auth.dto.SignupResponse;
import com.example.dateServer.auth.dto.TokenPairs;
import com.example.dateServer.auth.entity.User;
import com.example.dateServer.auth.dto.SignupRequest;
import jakarta.servlet.http.Cookie;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest signupRequest){
        User user = authService.signUp(signupRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SignupResponse.from(user));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        TokenPairs tokens = authService.login(loginRequest);


        ResponseCookie cookie = ResponseCookie.from("refreshToken",tokens.getRefreshToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(7*24*60*60)
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,cookie.toString())
                .body(LoginResponse.builder().accessToken(tokens.getAccessToken()).build());

    }
}
