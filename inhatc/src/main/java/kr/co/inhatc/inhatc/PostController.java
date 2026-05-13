package kr.co.inhatc.inhatc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import kr.co.inhatc.inhatc.dto.PostResponseDTO;
import kr.co.inhatc.inhatc.exception.CustomException;
import kr.co.inhatc.inhatc.exception.ErrorCode;
import kr.co.inhatc.inhatc.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Slf4j
@Validated
public class PostController {
    private final PostService postService;

    /**
     * 특정 사용자 게시글 조회 (memberId 기준)
     */
    @GetMapping("/user/id/{memberId}")
    public ResponseEntity<List<PostResponseDTO>> getPostsByMemberId(@PathVariable Long memberId) {
        return ResponseEntity.ok(postService.findByMemberId(memberId));
    }

    /**
     * 특정 사용자 게시글 조회 (memberName 기준)
     */
    @GetMapping("/user/name/{memberName}")
    public ResponseEntity<List<PostResponseDTO>> getPostsByMemberName(@PathVariable String memberName) {
        return ResponseEntity.ok(postService.findByMemberName(memberName));
    }

    /**
     * 전체 게시글 조회 (삭제되지 않은 것만, 페이징 지원)
     * 사용 예시:
     * - GET /api/posts?page=0&size=20 (첫 페이지, 20개씩)
     * - GET /api/posts?page=1&size=10&sort=createdDate,desc (두 번째 페이지, 10개씩, 최신순)
     */
    @GetMapping
    public ResponseEntity<Page<PostResponseDTO>> getAllPosts(
            @PageableDefault(size = 20, sort = "createdDate", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        Page<PostResponseDTO> posts = postService.findAll(pageable);
        log.debug("전체 게시글 조회: {}개 (페이지: {}, 크기: {})", 
                posts.getTotalElements(), posts.getNumber(), posts.getSize());
        return ResponseEntity.ok(posts);
    }

    /**
     * 삭제 여부 기준으로 게시글 조회
     */
    @GetMapping("/deleted/{deleteYn}")
    public ResponseEntity<List<PostResponseDTO>> getPostsByDeleteYn(@PathVariable char deleteYn) {
        return ResponseEntity.ok(postService.findAllByDeleteYn(deleteYn));
    }

    // 개별 게시글 조회
    @GetMapping("/{id}")
    public ResponseEntity<PostResponseDTO> getPostById(@PathVariable Long id) {
        PostResponseDTO post = postService.findById(id);
        return ResponseEntity.ok(post);
    }

    /**
     * 게시글 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id, HttpSession session) {
        String email = (String) session.getAttribute("loginEmail");
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // REST 개선: DELETE는 소유권 확인 후 본문 없이 204로 응답한다.
        postService.delete(id, email);
        return ResponseEntity.noContent().build();
    }

    /**
     * 좋아요 토글
     */
    @PostMapping("/{postId}/likes")
    public ResponseEntity<Map<String, Object>> toggleLove(
            @PathVariable Long postId,
            @RequestParam @NotBlank(message = "이메일은 필수입니다.") String email) {

        boolean liked = postService.toggleLove(postId, email);
        Map<String, Object> response = new HashMap<>();
        response.put("liked", liked);
        response.put("postId", postId);
        return ResponseEntity.ok(response);
    }

    /**
     * 팔로잉 중인 사용자들의 게시글 조회
     */
    @GetMapping("/following")
    public ResponseEntity<List<PostResponseDTO>> getFollowingPosts(
            @RequestParam @NotBlank(message = "이메일은 필수입니다.") String email) {
        List<PostResponseDTO> posts = postService.findFollowingPosts(email);
        log.debug("팔로잉 게시글 조회: {}개 (사용자: {})", posts.size(), email);
        return ResponseEntity.ok(posts);
    }

    /**
     * 게시글 업로드
     */
    // REST 개선: 게시글 생성은 컬렉션 리소스(POST /api/posts)로 받는다.
    @PostMapping({"", "/upload"})
    public ResponseEntity<String> uploadImage(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam("content") @NotBlank(message = "게시글 내용은 필수입니다.") 
            @Size(max = 2000, message = "게시글 내용은 2000자 이하여야 합니다.") String content,
            HttpSession session) {

        String email = (String) session.getAttribute("loginEmail");
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("로그인이 필요합니다.");
        }

        try {
            // 이미지 업로드 후 URL 반환
            String fileUrl = postService.imgupload(file, email);

            // 게시글 저장 (DB에는 URL 저장)
            postService.savePost(email, content, fileUrl);

            log.info(kr.co.inhatc.inhatc.constants.AppConstants.LogMessage.POST_UPLOAD_SUCCESS + ": 사용자={}, 파일={}", 
                    email, file != null ? file.getOriginalFilename() : "없음");
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(kr.co.inhatc.inhatc.constants.AppConstants.SuccessMessage.POST_UPLOAD_SUCCESS);
        } catch (IllegalArgumentException e) {
            log.warn(kr.co.inhatc.inhatc.constants.AppConstants.LogMessage.POST_UPLOAD_FAILED, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(String.format(kr.co.inhatc.inhatc.constants.AppConstants.ErrorMessage.POST_UPLOAD_FAILED, e.getMessage()));
        } catch (Exception e) {
            log.error("게시물 업로드 중 예외 발생: 사용자={}", email, e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

}
