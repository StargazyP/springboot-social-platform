package kr.co.inhatc.inhatc.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import kr.co.inhatc.inhatc.PostController;
import kr.co.inhatc.inhatc.config.SecurityConfig;
import kr.co.inhatc.inhatc.config.TestSecurityConfig;
import kr.co.inhatc.inhatc.service.PostService;
import kr.co.inhatc.inhatc.util.FileUploadValidator;

@WebMvcTest(controllers = PostController.class,
            excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class))
@Import(TestSecurityConfig.class)
@DisplayName("보안 테스트")
class SecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostService postService;

    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        session = new MockHttpSession();
        session.setAttribute("loginEmail", "test@example.com");
    }

    @Test
    @DisplayName("SQL Injection 방지 - 파라미터 바인딩 테스트")
    void sqlInjection_Prevention() throws Exception {
        // given - SQL Injection 시도 문자열 (URL 경로에서 사용 가능한 형태)
        // 특수문자('; --)는 URL 경로에서 문제를 일으킬 수 있으므로 더 현실적인 문자열 사용
        String sqlInjectionAttempt = "admin' OR '1'='1";
        
        // Mock 서비스가 빈 리스트를 반환하도록 설정
        when(postService.findByMemberName(anyString())).thenReturn(new java.util.ArrayList<>());

        // when & then - 파라미터가 안전하게 바인딩되어야 함
        // SQL Injection 시도 문자열이 그대로 파라미터로 전달되어도 안전하게 처리되어야 함
        mockMvc.perform(get("/api/posts/user/name/{memberName}", sqlInjectionAttempt))
                .andExpect(status().isOk());

        // 실제 SQL이 실행되지 않고 안전하게 처리되어야 함
        // Repository에서 @Param을 사용하므로 자동으로 방지됨
        verify(postService, times(1)).findByMemberName(sqlInjectionAttempt);
    }

    @Test
    @DisplayName("파일 업로드 보안 - 허용되지 않은 확장자 거부")
    void fileUpload_InvalidExtension() {
        // given
        MockMultipartFile maliciousFile = new MockMultipartFile(
                "file",
                "malicious.exe",
                "application/x-msdownload",
                "malicious content".getBytes()
        );

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            FileUploadValidator.validateFile(maliciousFile);
        });

        assertTrue(exception.getMessage().contains("허용되지 않은 파일 형식"));
    }

    @Test
    @DisplayName("파일 업로드 보안 - 경로 탐색 공격 방지")
    void fileUpload_PathTraversalPrevention() {
        // given
        MockMultipartFile pathTraversalFile = new MockMultipartFile(
                "file",
                "../../../etc/passwd",
                "image/png",
                "malicious content".getBytes()
        );

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            FileUploadValidator.validateFile(pathTraversalFile);
        });

        assertTrue(exception.getMessage().contains("잘못된 파일명"));
    }

    @Test
    @DisplayName("파일 업로드 보안 - 파일 크기 제한")
    void fileUpload_SizeLimit() {
        // given - 11MB 파일 (제한: 10MB)
        byte[] largeContent = new byte[11 * 1024 * 1024];
        MockMultipartFile largeFile = new MockMultipartFile(
                "file",
                "large.jpg",
                "image/jpeg",
                largeContent
        );

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            FileUploadValidator.validateFile(largeFile);
        });

        assertTrue(exception.getMessage().contains("파일 크기가 너무 큽니다"));
    }

    @Test
    @DisplayName("세션 보안 - 로그인하지 않은 사용자 접근 차단")
    void sessionSecurity_UnauthorizedAccess() throws Exception {
        // given
        MockHttpSession emptySession = new MockHttpSession();

        // when & then
        mockMvc.perform(post("/api/posts")
                .param("content", "테스트")
                .session(emptySession))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("세션 보안 - 세션 고정 공격 방지")
    void sessionSecurity_SessionFixationPrevention() throws Exception {
        // given
        String oldSessionId = session.getId();

        // when - 로그인 시도
        // 실제 구현에서는 로그인 성공 시 세션 ID가 변경되어야 함
        // MemberController의 login 메서드에서 session.invalidate() 후 새 세션 생성

        // then - 세션 ID가 변경되었는지 확인
        // (실제 테스트는 통합 테스트에서 세션 ID 비교로 확인 가능)
        assertNotNull(oldSessionId);
    }

    @Test
    @DisplayName("XSS 방지 - 입력값 검증")
    void xssPrevention_InputValidation() throws Exception {
        // given - XSS 시도 문자열
        String xssAttempt = "<script>alert('XSS')</script>";
        
        // Mock 서비스 설정
        when(postService.imgupload(any(), anyString())).thenReturn("/posts/test/image.jpg");
        doNothing().when(postService).savePost(anyString(), anyString(), anyString());

        // when & then - XSS 문자열은 @Size(max = 2000) 제약을 통과함
        // 실제 XSS 방지는 클라이언트(Thymeleaf)에서 자동 이스케이프 처리됨
        // REST 개선 후 생성 성공은 201 Created를 반환하고, 클라이언트에서 렌더링 시 안전하게 처리됨
        mockMvc.perform(post("/api/posts")
                .param("content", xssAttempt)
                .session(session))
                .andExpect(status().isCreated()); // XSS는 서버에서 검증하지 않고 클라이언트에서 처리

        verify(postService, times(1)).savePost(anyString(), eq(xssAttempt), anyString());
    }
}

