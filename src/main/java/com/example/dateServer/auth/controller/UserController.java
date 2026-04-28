package com.example.dateServer.auth.controller;

import com.example.dateServer.auth.dto.UserUpdateRequest;
import com.example.dateServer.auth.dto.UserResponse;
import com.example.dateServer.auth.service.UserService;
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
        return ResponseEntity.ok(userService.getMe(userId));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateMe(
            @AuthenticationPrincipal Long userId,
            @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateMe(userId, request));
    }
}
