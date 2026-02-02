package com.example.dateServer.like;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/matching")
public class MatchController {
    private final SwipeService swipeService;

    @PostMapping("/swipe")
    public ResponseEntity<?> swipe(@AuthenticationPrincipal Long userId,
                                   @RequestBody SwipeRequest request) {
        boolean matched = swipeService.swipeAndMatch(userId, request);
        if (matched) {
            return ResponseEntity.ok(Map.of("matched", true));
        }
        return ResponseEntity.ok().build();
    }
}
