package kr.co.inhatc.inhatc.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import kr.co.inhatc.inhatc.dto.CommentRequestDTO;
import kr.co.inhatc.inhatc.dto.CommentResponseDTO;
import kr.co.inhatc.inhatc.entity.CommentEntity;
import kr.co.inhatc.inhatc.entity.MemberEntity;
import kr.co.inhatc.inhatc.entity.PostEntity;
import kr.co.inhatc.inhatc.repository.CommentRepository;
import kr.co.inhatc.inhatc.repository.MemberRepository;
import kr.co.inhatc.inhatc.repository.PostRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class CommentService {

    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final NotificationService notificationService;

    // @Lazy를 생성자 파라미터에 적용하여 순환 참조 방지
    public CommentService(CommentRepository commentRepository,
                          MemberRepository memberRepository,
                          PostRepository postRepository,
                          @Lazy NotificationService notificationService) {
        this.commentRepository = commentRepository;
        this.memberRepository = memberRepository;
        this.postRepository = postRepository;
        this.notificationService = notificationService;
    }

    /**
     * ✅ 특정 이메일로 작성된 댓글 조회
     */
    public List<CommentResponseDTO> findByEmail(String email) {
        List<CommentEntity> list = commentRepository.findByWriterMemberEmail(email);
        return list.stream()
                .map(CommentResponseDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * ✅ 게시글별 댓글 전체 조회 (N+1 문제 해결, 계층적 구조 지원)
     */
    public List<CommentResponseDTO> getCommentsByPostId(Long postId) {
        // JOIN FETCH로 Writer와 Post를 한 번에 조회
        List<CommentEntity> allComments = commentRepository.findByPostIdWithWriter(postId);
        
        // 부모 댓글만 필터링 (parentComment가 null인 것들)
        List<CommentEntity> parentComments = allComments.stream()
                .filter(comment -> comment.getParentComment() == null)
                .collect(Collectors.toList());
        
        // 각 부모 댓글에 답글 추가
        return parentComments.stream()
                .map(parent -> {
                    CommentResponseDTO dto = new CommentResponseDTO(parent);
                    // 해당 부모 댓글의 답글들 찾기
                    List<CommentResponseDTO> replies = allComments.stream()
                            .filter(comment -> comment.getParentComment() != null 
                                    && comment.getParentComment().getId().equals(parent.getId()))
                            .map(CommentResponseDTO::new)
                            .collect(Collectors.toList());
                    dto.setReplies(replies);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * ✅ 게시글별 댓글 조회 (페이징 지원, N+1 문제 해결)
     */
    public Page<CommentResponseDTO> getCommentsByPostId(Long postId, Pageable pageable) {
        // @EntityGraph로 Writer와 Post를 한 번에 조회
        Page<CommentEntity> commentPage = commentRepository.findByPostIdOrderByCreateDateDesc(postId, pageable);
        return commentPage.map(CommentResponseDTO::new);
    }

    /**
     * ✅ 댓글 추가 (대댓글 지원)
     */
    public CommentResponseDTO addComment(CommentRequestDTO requestDTO) {
        log.debug("댓글 작성 요청: user={}, article={}, parentCommentId={}", 
                requestDTO.getUser(), requestDTO.getArticle(), requestDTO.getParentCommentId());

        MemberEntity writer = memberRepository.findByMemberEmail(requestDTO.getUser())
                .orElseThrow(() -> new RuntimeException("댓글 작성자를 찾을 수 없습니다."));

        PostEntity post = postRepository.findById(requestDTO.getArticle())
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        // 부모 댓글 조회 (대댓글인 경우)
        CommentEntity parentComment = null;
        if (requestDTO.getParentCommentId() != null) {
            parentComment = commentRepository.findById(requestDTO.getParentCommentId())
                    .orElseThrow(() -> new RuntimeException("부모 댓글을 찾을 수 없습니다."));
            // 부모 댓글이 같은 게시글에 속하는지 확인
            if (!parentComment.getPost().getId().equals(post.getId())) {
                throw new RuntimeException("부모 댓글과 게시글이 일치하지 않습니다.");
            }
        }

        CommentEntity comment = CommentEntity.builder()
                .comment(requestDTO.getComment())
                .writer(writer)
                .post(post)
                .parentComment(parentComment)
                .build();

        CommentEntity saved = commentRepository.save(comment);
        
        // 댓글 알림 생성 (대댓글이 아닌 경우에만)
        if (parentComment == null) {
            try {
                notificationService.createCommentNotification(post.getId(), requestDTO.getUser());
            } catch (Exception e) {
                // 알림 생성 실패해도 댓글은 정상 처리
                log.warn("알림 생성 실패: postId={}, user={}", post.getId(), requestDTO.getUser(), e);
            }
        }
        
        return new CommentResponseDTO(saved);
    }

    /**
     * ✅ 댓글 수정
     */
    public void updateComment(Long postId, Long commentId, CommentRequestDTO requestDTO) {
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글이 존재하지 않습니다."));

        // 게시글 불일치 방지
        if (!comment.getPost().getId().equals(postId)) {
            throw new RuntimeException("해당 댓글은 이 게시글에 속하지 않습니다.");
        }

        // 작성자 일치 여부 확인 (보안 강화)
        if (!comment.getWriter().getMemberEmail().equals(requestDTO.getUser())) {
            throw new RuntimeException("본인이 작성한 댓글만 수정할 수 있습니다.");
        }

        // 내용 수정 후 저장
        comment.update(requestDTO.getComment());
        commentRepository.save(comment);
    }

    /**
     * ✅ 댓글 삭제
     */
    public void deleteComment(Long postId, Long commentId, String requesterEmail) {
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글이 존재하지 않습니다."));

        // 게시글 불일치 방지
        if (!comment.getPost().getId().equals(postId)) {
            throw new RuntimeException("해당 댓글은 이 게시글에 속하지 않습니다.");
        }

        if (!comment.getWriter().getMemberEmail().equals(requesterEmail)) {
            throw new RuntimeException("본인이 작성한 댓글만 삭제할 수 있습니다.");
        }

        commentRepository.delete(comment);
    }
}
