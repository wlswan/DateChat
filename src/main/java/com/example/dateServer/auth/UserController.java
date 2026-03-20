package com.example.dateServer.auth;

import com.example.dateServer.auth.entity.User;
import com.example.dateServer.auth.exception.UserNotFoundException;
import com.example.dateServer.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal Long userId) {
        User me = userService.getMyInformation(userId);
        return ResponseEntity.ok(UserResponse.from(me));
    }

    @PatchMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal Long userId,
            @RequestBody ProfileUpdateRequest request) {
        UserResponse response = userService.updateProfile(userId, request);
        return ResponseEntity.ok(response);
    }
}
