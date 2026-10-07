package kr.co.inhatc.inhatc.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import kr.co.inhatc.inhatc.dto.CommentRequestDTO;
import kr.co.inhatc.inhatc.dto.CommentResponseDTO;
import kr.co.inhatc.inhatc.entity.CommentEntity;
import kr.co.inhatc.inhatc.entity.MemberEntity;
import kr.co.inhatc.inhatc.entity.PostEntity;
import kr.co.inhatc.inhatc.repository.CommentRepository;
import kr.co.inhatc.inhatc.repository.MemberRepository;
import kr.co.inhatc.inhatc.repository.PostRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentService 단위 테스트")
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private CommentService commentService;

    private MemberEntity testMember;
    private PostEntity testPost;
    private CommentEntity testComment;

    @BeforeEach
    void setUp() {
        testMember = MemberEntity.builder()
                .id(1L)
                .memberEmail("test@example.com")
                .memberName("테스트 사용자")
                .build();

        testPost = PostEntity.builder()
                .id(1L)
                .memberEmail("post@example.com")
                .content("테스트 게시글")
                .build();

        // CommentEntity는 Builder로 생성 (id는 자동 생성되므로 제외)
        testComment = CommentEntity.builder()
                .comment("테스트 댓글")
                .post(testPost)
                .writer(testMember)
                .build();
        // 테스트를 위해 id 설정 (리플렉션 사용)
        try {
            java.lang.reflect.Field idField = CommentEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(testComment, 1L);
        } catch (Exception e) {
            // 리플렉션 실패 시 무시
        }
    }

    @Test
    @DisplayName("게시글별 댓글 조회 성공")
    void getCommentsByPostId_Success() {
        // given
        Long postId = 1L;
        List<CommentEntity> comments = new ArrayList<>();
        comments.add(testComment);

        when(commentRepository.findByPostIdWithWriter(postId)).thenReturn(comments);

        // when
        List<CommentResponseDTO> result = commentService.getCommentsByPostId(postId);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(commentRepository, times(1)).findByPostIdWithWriter(postId);
    }

    @Test
    @DisplayName("페이징으로 댓글 조회 성공")
    void getCommentsByPostId_WithPaging() {
        // given
        Long postId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        List<CommentEntity> comments = new ArrayList<>();
        comments.add(testComment);
        Page<CommentEntity> commentPage = new PageImpl<>(comments, pageable, 1);

        when(commentRepository.findByPostIdOrderByCreateDateDesc(postId, pageable))
                .thenReturn(commentPage);

        // when
        Page<CommentResponseDTO> result = commentService.getCommentsByPostId(postId, pageable);

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(commentRepository, times(1)).findByPostIdOrderByCreateDateDesc(postId, pageable);
    }

    @Test
    @DisplayName("댓글 작성 성공")
    void addComment_Success() {
        // given
        CommentRequestDTO requestDTO = CommentRequestDTO.builder()
                .comment("새 댓글")
                .user("test@example.com")
                .article(1L)
                .build();

        when(memberRepository.findByMemberEmail("test@example.com"))
                .thenReturn(Optional.of(testMember));
        when(postRepository.findById(1L))
                .thenReturn(Optional.of(testPost));
        when(commentRepository.save(any(CommentEntity.class)))
                .thenReturn(testComment);

        // when
        CommentResponseDTO result = commentService.addComment(requestDTO);

        // then
        assertNotNull(result);
        verify(memberRepository, times(1)).findByMemberEmail("test@example.com");
        verify(postRepository, times(1)).findById(1L);
        verify(commentRepository, times(1)).save(any(CommentEntity.class));
    }

    @Test
    @DisplayName("댓글 수정 성공")
    void updateComment_Success() {
        // given
        Long postId = 1L;
        Long commentId = 1L;
        CommentRequestDTO requestDTO = CommentRequestDTO.builder()
                .comment("수정된 댓글")
                .user("test@example.com")
                .build();

        when(commentRepository.findById(commentId))
                .thenReturn(Optional.of(testComment));
        when(commentRepository.save(any(CommentEntity.class)))
                .thenReturn(testComment);

        // when
        commentService.updateComment(postId, commentId, requestDTO);

        // then
        verify(commentRepository, times(1)).findById(commentId);
        verify(commentRepository, times(1)).save(any(CommentEntity.class));
    }

    @Test
    @DisplayName("댓글 삭제 성공")
    void deleteComment_Success() {
        // given
        Long postId = 1L;
        Long commentId = 1L;

        when(commentRepository.findById(commentId))
                .thenReturn(Optional.of(testComment));

        // when
        commentService.deleteComment(postId, commentId, "test@example.com");

        // then
        verify(commentRepository, times(1)).findById(commentId);
        verify(commentRepository, times(1)).delete(testComment);
    }

    @Test
    @DisplayName("대댓글 1단이 루트 replies에 포함된다")
    void getCommentsByPostId_IncludesDirectReplies() throws Exception {
        Long postId = 1L;
        CommentEntity root = testComment;
        CommentEntity reply = CommentEntity.builder()
                .comment("1단 답글")
                .post(testPost)
                .writer(testMember)
                .parentComment(root)
                .build();
        setId(reply, 2L);

        when(commentRepository.findByPostIdWithWriter(postId))
                .thenReturn(List.of(root, reply));

        List<CommentResponseDTO> result = commentService.getCommentsByPostId(postId);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getReplies().size());
        assertEquals(2L, result.get(0).getReplies().get(0).getId());
        assertEquals(1L, result.get(0).getReplies().get(0).getParentCommentId());
    }

    @Test
    @DisplayName("2단 이상 대댓글도 트리 replies에 포함된다 (유튜브식 스레드)")
    void getCommentsByPostId_IncludesNestedReplies() throws Exception {
        Long postId = 1L;
        CommentEntity root = testComment;
        CommentEntity reply = CommentEntity.builder()
                .comment("1단 답글")
                .post(testPost)
                .writer(testMember)
                .parentComment(root)
                .build();
        setId(reply, 2L);
        CommentEntity nested = CommentEntity.builder()
                .comment("2단 답글")
                .post(testPost)
                .writer(testMember)
                .parentComment(reply)
                .build();
        setId(nested, 3L);

        when(commentRepository.findByPostIdWithWriter(postId))
                .thenReturn(List.of(root, reply, nested));

        List<CommentResponseDTO> result = commentService.getCommentsByPostId(postId);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getReplies().size());
        CommentResponseDTO depth1 = result.get(0).getReplies().get(0);
        assertEquals(2L, depth1.getId());
        assertEquals(1, depth1.getReplies().size());
        assertEquals(3L, depth1.getReplies().get(0).getId());
        assertEquals(2L, depth1.getReplies().get(0).getParentCommentId());
    }

    @Test
    @DisplayName("대댓글 작성 시 parentComment가 저장된다")
    void addComment_WithParent_Success() {
        CommentRequestDTO requestDTO = CommentRequestDTO.builder()
                .comment("답글")
                .user("test@example.com")
                .article(1L)
                .parentCommentId(1L)
                .build();

        CommentEntity savedReply = CommentEntity.builder()
                .comment("답글")
                .post(testPost)
                .writer(testMember)
                .parentComment(testComment)
                .build();
        try {
            setId(savedReply, 10L);
        } catch (Exception ignored) {
        }

        when(memberRepository.findByMemberEmail("test@example.com"))
                .thenReturn(Optional.of(testMember));
        when(postRepository.findById(1L))
                .thenReturn(Optional.of(testPost));
        when(commentRepository.findById(1L))
                .thenReturn(Optional.of(testComment));
        when(commentRepository.save(any(CommentEntity.class)))
                .thenReturn(savedReply);

        CommentResponseDTO result = commentService.addComment(requestDTO);

        assertNotNull(result);
        assertEquals(1L, result.getParentCommentId());
        verify(notificationService, never()).createCommentNotification(anyLong(), anyString());
    }

    private static void setId(CommentEntity entity, Long id) throws Exception {
        java.lang.reflect.Field idField = CommentEntity.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(entity, id);
    }
}

