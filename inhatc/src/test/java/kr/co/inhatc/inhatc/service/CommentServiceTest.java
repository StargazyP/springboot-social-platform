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
}

