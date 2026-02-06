package com.example.dateServer.like;

import com.example.dateServer.auth.entity.User;
import com.example.dateServer.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SwipeService {
    private final SwipeRepository swipeRepository;
    private final UserRepository userRepository;
    private final MatchRepository matchRepository;

    @Transactional
    public boolean swipeAndMatch(Long fromUserId,SwipeRequest request) {
        User fromUser = userRepository.findById(fromUserId).orElseThrow(IllegalArgumentException::new);
        User toUser = userRepository.findById(request.getToUserId()).orElseThrow(IllegalArgumentException::new);

        if(swipeRepository.existsByFromUserAndToUser(fromUser,toUser)) {
            throw new IllegalStateException();
        }

        Swipe swipe = Swipe.builder()
                .fromUser(fromUser)
                .toUser(toUser)
                .type(request.getType())
                .build();
        Swipe savedSwipe = swipeRepository.save(swipe);

        if(savedSwipe.getType() == SwipeType.LIKE && swipeRepository.existsByFromUserAndToUserAndType(toUser, fromUser, SwipeType.LIKE)) {
            Match match = Match.builder().user1(fromUser)
                    .user2(toUser)
                    .build();
            matchRepository.save(match);
            return true;
        }
        return false;
    }
}
