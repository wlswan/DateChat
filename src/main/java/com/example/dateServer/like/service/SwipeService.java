package com.example.dateServer.like.service;

import com.example.dateServer.auth.entity.User;
import com.example.dateServer.auth.entity.UserPreference;
import com.example.dateServer.auth.exception.UserNotFoundException;
import com.example.dateServer.auth.exception.UserPreferenceNotFoundException;
import com.example.dateServer.auth.repository.UserRepository;
import com.example.dateServer.chat.entity.ChatRoom;
import com.example.dateServer.chat.repository.ChatRoomRepository;
import com.example.dateServer.like.dto.*;
import com.example.dateServer.like.entity.Match;
import com.example.dateServer.like.entity.Swipe;
import com.example.dateServer.like.entity.SwipeType;
import com.example.dateServer.like.repository.MatchRepository;
import com.example.dateServer.like.repository.SwipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
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
    private final ChatRoomRepository chatRoomRepository;

    @Transactional(readOnly = true)
    public List<UserProfileResponse> getDiscoverUsers(Long userId) {
        User user = userRepository.findByIdWithPreference(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        UserPreference pref = user.getUserPreference();
        if (pref == null) throw new UserPreferenceNotFoundException(userId);

        return userRepository.findDiscoverCandidates(userId
                        , user.getGender()
                        , user.getLang()
                        , pref.getMinAge()
                        , pref.getMaxAge()).stream()
                .map(UserProfileResponse::from)
                .toList();
    }

    @Transactional
    public SwipeResult swipeAndMatch(Long fromUserId, SwipeRequest request) {
        Long toUserId = request.getToUserId();

        if (swipeRepository.existsByFromUser_IdAndToUser_Id(fromUserId, toUserId)) {
            throw new IllegalStateException();
        }

        User fromUser = userRepository.getReferenceById(fromUserId); //프록시
        User toUser = userRepository.getReferenceById(toUserId);

        Swipe swipe = Swipe.builder()
                .fromUser(fromUser)
                .toUser(toUser)
                .type(request.getType())
                .build();
        Swipe savedSwipe = swipeRepository.save(swipe);

        if (savedSwipe.getType() == SwipeType.LIKE && swipeRepository.existsByFromUser_IdAndToUser_IdAndType(toUserId, fromUserId, SwipeType.LIKE)) {
            return createMatchSafely(fromUser, toUser);
        }
        return new SwipeResult(false, null);
    }

    private SwipeResult createMatchSafely(User fromUser, User toUser) {
        User user1 = fromUser.getId() < toUser.getId() ? fromUser : toUser;
        User user2 = fromUser.getId() < toUser.getId() ? toUser : fromUser;

        try {
            Match match = Match.builder()
                    .user1(user1)
                    .user2(user2)
                    .build();
            Match savedMatch = matchRepository.save(match);

            ChatRoom chatRoom = ChatRoom.builder()
                    .match(savedMatch)
                    .build();
            ChatRoom savedRoom = chatRoomRepository.save(chatRoom);

            return new SwipeResult(true, savedRoom.getId());
        } catch (DataIntegrityViolationException e) {
            ChatRoom existingRoom = chatRoomRepository.findByUserIds(fromUser.getId(), toUser.getId())
                    .orElseThrow(() -> new IllegalStateException("채팅방 생성 중 오류 발생"));
            return new SwipeResult(true, existingRoom.getId());
        }
    }

    public List<MatchResponse> getMatches(Long userId) {
        return matchRepository.findMatchesWithUsersByUserId(userId).stream()
                .map(match -> {
                    User partner = userId.equals(match.getUser1().getId()) ? match.getUser2() : match.getUser1();
                    return new MatchResponse(match.getId()
                            , partner.getId()
                            , partner.getNickname()
                            , partner.getLang()
                            , partner.getProfileImageUrl()
                            , match.getCreatedAt());
                }).toList();
    }

    public MatchDetailResponse getMatchDetail(Long userId, Long matchId) {
        Match match = matchRepository.findByIdWithUsers(matchId).orElseThrow(() -> new IllegalArgumentException("매칭이 없습니다."));
        if (!userId.equals(match.getUser1().getId()) && !userId.equals(match.getUser2().getId())) {
            throw new IllegalArgumentException("접근 권한 없음");
        }
        User partnerUser = userId.equals(match.getUser1().getId()) ? match.getUser2() : match.getUser1();
        return MatchDetailResponse.from(partnerUser, match);
    }
}
