package com.example.dateServer.profile;

import com.example.dateServer.profile.dto.ProfileResponse;
import com.example.dateServer.profile.dto.ProfileUpdateRequest;
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
    public ResponseEntity<ProfileResponse> getMyProfile(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(userService.getProfile(userId));
    }

    @PutMapping("/me")
    public ResponseEntity<ProfileResponse> updateProfile(
            @AuthenticationPrincipal Long userId,
            @RequestBody ProfileUpdateRequest request) {
        return ResponseEntity.ok(userService.updateProfile(userId, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfileResponse> getProfile(
            @PathVariable Long id) {
        return ResponseEntity.ok(userService.getProfile(id));
    }
}
