package com.example.dateServer.like.controller;

import com.example.dateServer.like.dto.*;
import com.example.dateServer.like.service.SwipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/matching")
public class MatchController {
    private final SwipeService swipeService;

    @GetMapping("/discover")
    public ResponseEntity<List<UserProfileResponse>> getDiscoverUsers(@AuthenticationPrincipal Long userId) {
        List<UserProfileResponse> users = swipeService.getDiscoverUsers(userId);
        return ResponseEntity.ok(users);
    }

    @PostMapping("/swipe")
    public ResponseEntity<?> swipe(@AuthenticationPrincipal Long userId,
                                   @RequestBody SwipeRequest request) {
        SwipeResult result = swipeService.swipeAndMatch(userId, request);
        if (result.isMatched()) {
            return ResponseEntity.ok(Map.of("matched", true, "roomId", result.getRoomId()));
        }
        return ResponseEntity.ok(Map.of("matched", false));
    }

    @GetMapping("/matches")
    public ResponseEntity<?> getMatches(@AuthenticationPrincipal Long userId) {
        List<MatchResponse> matches = swipeService.getMatches(userId);
        return ResponseEntity.ok(matches);
    }

    @GetMapping("/matches/{matchId}")
    public ResponseEntity<?> getMatchInfo(@AuthenticationPrincipal Long userId,
                                          @PathVariable Long matchId) {
        MatchDetailResponse response = swipeService.getMatchDetail(userId, matchId);
        return ResponseEntity.ok(response);

    }
}
