package kr.co.inhatc.inhatc.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import kr.co.inhatc.inhatc.PostController;
import kr.co.inhatc.inhatc.config.SecurityConfig;
import kr.co.inhatc.inhatc.config.TestSecurityConfig;
import kr.co.inhatc.inhatc.dto.PostResponseDTO;
import kr.co.inhatc.inhatc.service.PostService;

@WebMvcTest(controllers = PostController.class,
            excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class))
@Import(TestSecurityConfig.class)
@DisplayName("PostController 통합 테스트")
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostService postService;

    private MockHttpSession session;
    private PostResponseDTO testPostDTO;

    @BeforeEach
    void setUp() {
        session = new MockHttpSession();
        session.setAttribute("loginEmail", "test@example.com");

        testPostDTO = PostResponseDTO.builder()
                .id(1L)
                .content("테스트 게시글")
                .writerName("테스트 사용자")
                .build();
    }

    @Test
    @DisplayName("전체 게시글 조회 성공")
    void getAllPosts_Success() throws Exception {
        // given
        List<PostResponseDTO> posts = new ArrayList<>();
        posts.add(testPostDTO);
        Page<PostResponseDTO> postPage = new PageImpl<>(posts);

        when(postService.findAll(any(Pageable.class))).thenReturn(postPage);

        // when & then
        mockMvc.perform(get("/api/posts")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1L));

        verify(postService, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("게시글 ID로 조회 성공")
    void getPostById_Success() throws Exception {
        // given
        Long postId = 1L;
        when(postService.findById(postId)).thenReturn(testPostDTO);

        // when & then
        mockMvc.perform(get("/api/posts/{id}", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.content").value("테스트 게시글"));

        verify(postService, times(1)).findById(postId);
    }

    @Test
    @DisplayName("게시글 업로드 성공")
    void uploadImage_Success() throws Exception {
        // given
        when(postService.imgupload(any(), anyString())).thenReturn("/posts/test/image.jpg");
        doNothing().when(postService).savePost(anyString(), anyString(), anyString());

        // when & then
        mockMvc.perform(multipart("/api/posts")
                .file("file", "test image content".getBytes())
                .param("content", "테스트 게시글 내용")
                .session(session))
                .andExpect(status().isCreated());

        verify(postService, times(1)).imgupload(any(), eq("test@example.com"));
        verify(postService, times(1)).savePost(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("게시글 업로드 실패 - 로그인하지 않음")
    void uploadImage_Fail_NotLoggedIn() throws Exception {
        // given
        MockHttpSession emptySession = new MockHttpSession();

        // when & then
        mockMvc.perform(multipart("/api/posts")
                .file("file", "test image content".getBytes())
                .param("content", "테스트 게시글 내용")
                .session(emptySession))
                .andExpect(status().isUnauthorized());

        verify(postService, never()).imgupload(any(), anyString());
    }

    @Test
    @DisplayName("게시글 삭제 성공")
    void deletePost_Success() throws Exception {
        // given
        Long postId = 1L;
        doNothing().when(postService).delete(eq(postId), eq("test@example.com"));

        // when & then
        mockMvc.perform(delete("/api/posts/{id}", postId).session(session))
                .andExpect(status().isNoContent());

        verify(postService, times(1)).delete(postId, "test@example.com");
    }
}

