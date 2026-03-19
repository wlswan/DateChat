package com.example.dateServer.like.service;

import com.example.dateServer.auth.entity.User;
import com.example.dateServer.auth.exception.UserNotFoundException;
import com.example.dateServer.auth.repository.UserRepository;
import com.example.dateServer.like.dto.SwipeRequest;
import com.example.dateServer.like.dto.SwipeResult;
import com.example.dateServer.like.dto.UserProfileResponse;
import com.example.dateServer.like.entity.Match;
import com.example.dateServer.like.entity.Swipe;
import com.example.dateServer.like.entity.SwipeType;
import com.example.dateServer.like.repository.MatchRepository;
import com.example.dateServer.like.repository.SwipeRepository;
import com.example.dateServer.redis.RoomLangCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SwipeService {
    private final SwipeRepository swipeRepository;
    private final UserRepository userRepository;
    private final MatchRepository matchRepository;
    private final RoomLangCacheService roomLangCacheService;

    @Transactional(readOnly = true)
    public List<UserProfileResponse> getDiscoverUsers(Long userId) {
        List<Long> swipedUserIds = swipeRepository.findSwipedUserIdsByMe(userId);

        List<User> candidates;
        if (swipedUserIds.isEmpty()) {
            candidates = userRepository.findAllExceptUser(userId);
        } else {
            candidates = userRepository.findDiscoverCandidates(userId, swipedUserIds);
        }

        return candidates.stream()
                .map(UserProfileResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public SwipeResult swipeAndMatch(Long fromUserId, SwipeRequest request) {
        User fromUser = userRepository.findById(fromUserId).orElseThrow(()-> new UserNotFoundException(fromUserId));
        User toUser = userRepository.findById(request.getToUserId()).orElseThrow(() -> new UserNotFoundException(request.getToUserId()));

        if(swipeRepository.existsByFromUserAndToUser(fromUser,toUser)) {
            throw new IllegalStateException();
        }

        Swipe swipe = Swipe.builder()
                .fromUser(fromUser)
                .toUser(toUser)
                .type(request.getType()) //pass or like
                .build();
        Swipe savedSwipe = swipeRepository.save(swipe);

        if(savedSwipe.getType() == SwipeType.LIKE && swipeRepository.existsByFromUserAndToUserAndType(toUser, fromUser, SwipeType.LIKE)) {
            Match match = Match.builder().user1(fromUser)
                    .user2(toUser)
                    .build();
            Match savedMatch = matchRepository.save(match);
            roomLangCacheService.save(savedMatch.getId(),fromUser,toUser);
            return new SwipeResult(true, savedMatch.getId());
        }
        return new SwipeResult(false, null);
    }
}
