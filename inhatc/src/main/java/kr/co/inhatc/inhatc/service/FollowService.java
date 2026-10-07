package kr.co.inhatc.inhatc.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.inhatc.inhatc.dto.FollowDTO;
import kr.co.inhatc.inhatc.dto.FollowStatsDTO;
import kr.co.inhatc.inhatc.dto.MemberDTO;
import kr.co.inhatc.inhatc.entity.FollowEntity;
import kr.co.inhatc.inhatc.entity.MemberEntity;
import kr.co.inhatc.inhatc.repository.FollowRepository;
import kr.co.inhatc.inhatc.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FollowService {

    private final FollowRepository followRepository;
    private final MemberRepository memberRepository;

    /**
     * 팔로우하기
     */
    @Transactional
    public void follow(String followerEmail, String followingEmail) {
        if (followerEmail.equals(followingEmail)) {
            throw new IllegalArgumentException("자기 자신을 팔로우할 수 없습니다.");
        }

        MemberEntity follower = memberRepository.findByMemberEmail(followerEmail)
                .orElseThrow(() -> new RuntimeException("팔로우하는 사용자를 찾을 수 없습니다."));
        
        MemberEntity following = memberRepository.findByMemberEmail(followingEmail)
                .orElseThrow(() -> new RuntimeException("팔로우할 사용자를 찾을 수 없습니다."));

        // 이미 팔로우 중인지 확인
        if (followRepository.existsByFollowerAndFollowing(follower, following)) {
            throw new IllegalArgumentException("이미 팔로우 중인 사용자입니다.");
        }

        FollowEntity followEntity = new FollowEntity(follower, following);
        followRepository.save(followEntity);
        log.info("팔로우 성공: {} -> {}", followerEmail, followingEmail);
    }

    /**
     * 언팔로우하기
     */
    @Transactional
    public void unfollow(String followerEmail, String followingEmail) {
        MemberEntity follower = memberRepository.findByMemberEmail(followerEmail)
                .orElseThrow(() -> new RuntimeException("언팔로우하는 사용자를 찾을 수 없습니다."));
        
        MemberEntity following = memberRepository.findByMemberEmail(followingEmail)
                .orElseThrow(() -> new RuntimeException("언팔로우할 사용자를 찾을 수 없습니다."));

        FollowEntity followEntity = followRepository.findByFollowerAndFollowing(follower, following)
                .orElseThrow(() -> new RuntimeException("팔로우 관계가 존재하지 않습니다."));

        followRepository.delete(followEntity);
        log.info("언팔로우 성공: {} -> {}", followerEmail, followingEmail);
    }

    /**
     * 팔로우 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean isFollowing(String followerEmail, String followingEmail) {
        if (followerEmail == null || followingEmail == null || followerEmail.equals(followingEmail)) {
            return false;
        }

        MemberEntity follower = memberRepository.findByMemberEmail(followerEmail).orElse(null);
        MemberEntity following = memberRepository.findByMemberEmail(followingEmail).orElse(null);

        if (follower == null || following == null) {
            return false;
        }

        return followRepository.existsByFollowerAndFollowing(follower, following);
    }

    /**
     * 여러 사용자에 대한 팔로우 여부 일괄 조회
     */
    @Transactional(readOnly = true)
    public Map<String, Boolean> getFollowingStatusBatch(String followerEmail, List<String> targetEmails) {
        Map<String, Boolean> result = new HashMap<>();
        if (followerEmail == null || targetEmails == null || targetEmails.isEmpty()) {
            return result;
        }

        List<String> distinctEmails = targetEmails.stream()
                .filter(email -> email != null && !email.equals(followerEmail))
                .distinct()
                .collect(Collectors.toList());

        distinctEmails.forEach(email -> result.put(email, false));
        if (distinctEmails.isEmpty()) {
            return result;
        }

        MemberEntity follower = memberRepository.findByMemberEmail(followerEmail).orElse(null);
        if (follower == null) {
            return result;
        }

        List<String> followedEmails = followRepository.findFollowingEmails(follower, distinctEmails);
        followedEmails.forEach(email -> result.put(email, true));
        return result;
    }

    /**
     * 팔로워 목록 조회
     */
    @Transactional(readOnly = true)
    public List<FollowDTO> getFollowers(String memberEmail, String currentUserEmail) {
        MemberEntity member = memberRepository.findByMemberEmail(memberEmail)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        List<FollowEntity> follows = followRepository.findByFollowing(member);
        
        return follows.stream()
                .map(follow -> {
                    MemberEntity follower = follow.getFollower();
                    MemberDTO followerDTO = MemberEntity.toDTO(follower);
                    return FollowDTO.builder()
                            .id(follow.getId())
                            .followerEmail(followerDTO.getMemberEmail())
                            .followerName(followerDTO.getMemberName())
                            .followerProfilePicturePath(followerDTO.getProfilePicturePath())
                            .isFollowing(currentUserEmail != null && 
                                       isFollowing(currentUserEmail, followerDTO.getMemberEmail()))
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 팔로잉 목록 조회
     */
    @Transactional(readOnly = true)
    public List<FollowDTO> getFollowing(String memberEmail, String currentUserEmail) {
        MemberEntity member = memberRepository.findByMemberEmail(memberEmail)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        List<FollowEntity> follows = followRepository.findByFollower(member);
        
        return follows.stream()
                .map(follow -> {
                    MemberEntity following = follow.getFollowing();
                    MemberDTO followingDTO = MemberEntity.toDTO(following);
                    return FollowDTO.builder()
                            .id(follow.getId())
                            .followingEmail(followingDTO.getMemberEmail())
                            .followingName(followingDTO.getMemberName())
                            .followingProfilePicturePath(followingDTO.getProfilePicturePath())
                            .isFollowing(currentUserEmail != null && 
                                       isFollowing(currentUserEmail, followingDTO.getMemberEmail()))
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 팔로우 통계 조회
     */
    @Transactional(readOnly = true)
    public FollowStatsDTO getFollowStats(String memberEmail, String currentUserEmail) {
        MemberEntity member = memberRepository.findByMemberEmail(memberEmail)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Long followerCount = followRepository.countFollowersByFollowing(member);
        Long followingCount = followRepository.countFollowingByFollower(member);
        boolean isFollowing = currentUserEmail != null && 
                             isFollowing(currentUserEmail, memberEmail);

        return FollowStatsDTO.builder()
                .followerCount(followerCount)
                .followingCount(followingCount)
                .isFollowing(isFollowing)
                .build();
    }
}




