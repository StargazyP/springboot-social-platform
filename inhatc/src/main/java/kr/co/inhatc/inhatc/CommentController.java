package kr.co.inhatc.inhatc;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import kr.co.inhatc.inhatc.dto.CommentRequestDTO;
import kr.co.inhatc.inhatc.dto.CommentResponseDTO;
import kr.co.inhatc.inhatc.service.CommentService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 특정 게시글의 모든 댓글 조회 (페이징 지원)
     * GET /api/posts/{postId}/comments
     * GET /api/posts/{postId}/comments?page=0&size=20 (페이징 사용)
     * GET /api/posts/{postId}/comments?all=true (전체 조회, 페이징 없음)
     */
    @GetMapping("/{postId}/comments")
    public ResponseEntity<?> getComments(
            @PathVariable Long postId,
            @RequestParam(required = false, defaultValue = "false") boolean all,
            @PageableDefault(size = 20, sort = "createDate", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        if (all) {
            // 전체 조회 (기존 방식, 하위 호환성 유지)
            List<CommentResponseDTO> comments = commentService.getCommentsByPostId(postId);
            return ResponseEntity.ok(comments);
        } else {
            // 페이징 조회
            Page<CommentResponseDTO> commentPage = commentService.getCommentsByPostId(postId, pageable);
            return ResponseEntity.ok(commentPage);
        }
    }

    /**
     * 댓글 작성
     * POST /api/posts/{postId}/comments
     */
    @PostMapping("/{postId}/comments")
    public ResponseEntity<CommentResponseDTO> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentRequestDTO commentRequestDTO,
            HttpSession session) {

        // 로그인된 사용자 이메일을 세션에서 가져옴
        String loginEmail = (String) session.getAttribute("loginEmail");
        if (loginEmail == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // DTO에 세팅
        commentRequestDTO.setUser(loginEmail);
        commentRequestDTO.setArticle(postId);

        CommentResponseDTO savedComment = commentService.addComment(commentRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedComment);
    }

    // REST 개선: 수정/삭제 주체는 요청 본문이 아니라 세션 사용자로 검증한다.
    @PutMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<String> updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequestDTO requestDTO,
            HttpSession session
    ) {
        String loginEmail = (String) session.getAttribute("loginEmail");
        if (loginEmail == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        requestDTO.setUser(loginEmail);
        commentService.updateComment(postId, commentId, requestDTO);
        return ResponseEntity.ok("댓글이 수정되었습니다.");
    }

        /**
     * 댓글 삭제
     * DELETE /api/posts/{postId}/comments/{commentId}
     */
    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<String> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            HttpSession session
    ) {
        String loginEmail = (String) session.getAttribute("loginEmail");
        if (loginEmail == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        commentService.deleteComment(postId, commentId, loginEmail);
        return ResponseEntity.noContent().build();
    }

}
