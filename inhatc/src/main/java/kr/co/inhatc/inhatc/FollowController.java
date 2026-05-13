package kr.co.inhatc.inhatc;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;
import kr.co.inhatc.inhatc.dto.FollowDTO;
import kr.co.inhatc.inhatc.dto.FollowStatsDTO;
import kr.co.inhatc.inhatc.service.FollowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class FollowController {

    private final FollowService followService;

    /**
     * 팔로우하기
     */
    // REST 개선: 팔로우 관계를 현재 사용자의 following 하위 리소스로 표현한다.
    @PostMapping({"/members/me/following/{followingEmail}", "/follow/{followingEmail}"})
    public ResponseEntity<String> follow(
            @PathVariable String followingEmail,
            HttpSession session) {
        String followerEmail = (String) session.getAttribute("loginEmail");
        
        if (followerEmail == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("로그인이 필요합니다.");
        }

        try {
            followService.follow(followerEmail, followingEmail);
            return ResponseEntity.ok("팔로우 성공");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    /**
     * 언팔로우하기
     */
    @DeleteMapping("/members/me/following/{followingEmail}")
    public ResponseEntity<Void> deleteFollowing(
            @PathVariable String followingEmail,
            HttpSession session) {
        ResponseEntity<String> result = unfollow(followingEmail, session);
        return ResponseEntity.status(result.getStatusCode()).build();
    }

    @PostMapping("/follow/{followingEmail}/unfollow")
    public ResponseEntity<String> unfollow(
            @PathVariable String followingEmail,
            HttpSession session) {
        String followerEmail = (String) session.getAttribute("loginEmail");
        
        if (followerEmail == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("로그인이 필요합니다.");
        }

        try {
            followService.unfollow(followerEmail, followingEmail);
            return ResponseEntity.ok("언팔로우 성공");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    /**
     * 팔로우 여부 확인
     */
    @GetMapping({"/members/me/following/{followingEmail}/status", "/follow/{followingEmail}/status"})
    public ResponseEntity<Boolean> isFollowing(
            @PathVariable String followingEmail,
            HttpSession session) {
        String followerEmail = (String) session.getAttribute("loginEmail");
        
        if (followerEmail == null) {
            return ResponseEntity.ok(false);
        }

        boolean isFollowing = followService.isFollowing(followerEmail, followingEmail);
        return ResponseEntity.ok(isFollowing);
    }

    /**
     * 팔로워 목록 조회
     */
    @GetMapping({"/members/{memberEmail}/followers", "/follow/{memberEmail}/followers"})
    public ResponseEntity<List<FollowDTO>> getFollowers(
            @PathVariable String memberEmail,
            HttpSession session) {
        String currentUserEmail = (String) session.getAttribute("loginEmail");
        
        try {
            List<FollowDTO> followers = followService.getFollowers(memberEmail, currentUserEmail);
            return ResponseEntity.ok(followers);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * 팔로잉 목록 조회
     */
    @GetMapping({"/members/{memberEmail}/following", "/follow/{memberEmail}/following"})
    public ResponseEntity<List<FollowDTO>> getFollowing(
            @PathVariable String memberEmail,
            HttpSession session) {
        String currentUserEmail = (String) session.getAttribute("loginEmail");
        
        try {
            List<FollowDTO> following = followService.getFollowing(memberEmail, currentUserEmail);
            return ResponseEntity.ok(following);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * 팔로우 통계 조회
     */
    @GetMapping({"/members/{memberEmail}/follow-stats", "/follow/{memberEmail}/stats"})
    public ResponseEntity<FollowStatsDTO> getFollowStats(
            @PathVariable String memberEmail,
            HttpSession session) {
        String currentUserEmail = (String) session.getAttribute("loginEmail");
        
        try {
            FollowStatsDTO stats = followService.getFollowStats(memberEmail, currentUserEmail);
            return ResponseEntity.ok(stats);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}

