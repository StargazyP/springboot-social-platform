package kr.co.inhatc.inhatc.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
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
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.co.inhatc.inhatc.MemberController;
import kr.co.inhatc.inhatc.config.SecurityConfig;
import kr.co.inhatc.inhatc.config.TestSecurityConfig;
import kr.co.inhatc.inhatc.dto.LoginRequest;
import kr.co.inhatc.inhatc.dto.MemberDTO;
import kr.co.inhatc.inhatc.service.MemberService;

@WebMvcTest(controllers = MemberController.class,
            excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class))
@Import(TestSecurityConfig.class)
@DisplayName("MemberController 통합 테스트")
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MemberService memberService;

    private MemberDTO testMemberDTO;

    @BeforeEach
    void setUp() {
        testMemberDTO = MemberDTO.builder()
                .id(1L)
                .memberEmail("test@example.com")
                .memberName("테스트 사용자")
                .build();
    }

    @Test
    @DisplayName("회원가입 페이지 조회")
    void signupForm_Success() throws Exception {
        mockMvc.perform(get("/api/members/signup"))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"));
    }

    @Test
    @DisplayName("회원가입 성공 - HTML 폼")
    void signup_Success() throws Exception {
        doNothing().when(memberService).save(any(MemberDTO.class));

        mockMvc.perform(post("/api/members/signup")
                .contentType("application/x-www-form-urlencoded")
                .param("email", "new@example.com")
                .param("password", "password123")
                .param("name", "신규 사용자"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(memberService, times(1)).save(any(MemberDTO.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 중복 이메일")
    void signup_Fail_DuplicateEmail() throws Exception {
        doThrow(new IllegalArgumentException("이미 사용 중인 이메일입니다."))
                .when(memberService).save(any(MemberDTO.class));

        mockMvc.perform(post("/api/members/signup")
                .contentType("application/x-www-form-urlencoded")
                .param("email", "test@example.com")
                .param("password", "password123")
                .param("name", "테스트"))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"))
                .andExpect(model().attribute("error", "이미 사용 중인 이메일입니다."));
    }

    @Test
    @DisplayName("회원가입 성공 - JSON API")
    void signupApi_Success() throws Exception {
        doNothing().when(memberService).save(any(MemberDTO.class));

        MemberDTO request = MemberDTO.builder()
                .memberEmail("api@example.com")
                .memberPassword("password123")
                .memberName("API 사용자")
                .build();

        mockMvc.perform(post("/api/members/signup")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("회원가입이 완료되었습니다."))
                .andExpect(jsonPath("$.memberEmail").value("api@example.com"));
    }

    @Test
    @DisplayName("로그인 성공")
    void login_Success() throws Exception {
        // given
        when(memberService.login("test@example.com", "password123"))
                .thenReturn(testMemberDTO);

        // when & then
        mockMvc.perform(post("/api/members/login")
                .contentType("application/x-www-form-urlencoded")
                .param("email", "test@example.com")
                .param("password", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/main"));

        verify(memberService, times(1)).login("test@example.com", "password123");
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 자격증명")
    void login_Fail_WrongCredentials() throws Exception {
        // given
        when(memberService.login("test@example.com", "wrongpassword"))
                .thenReturn(null);

        // when & then
        mockMvc.perform(post("/api/members/login")
                .contentType("application/x-www-form-urlencoded")
                .param("email", "test@example.com")
                .param("password", "wrongpassword"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"));

        verify(memberService, times(1)).login("test@example.com", "wrongpassword");
    }

    @Test
    @DisplayName("로그인 성공 - JSON API")
    void loginJson_Success() throws Exception {
        when(memberService.login("test@example.com", "password123"))
                .thenReturn(testMemberDTO);

        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        mockMvc.perform(post("/api/members/login")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loginEmail").value("test@example.com"))
                .andExpect(jsonPath("$.redirect").value("/main"));

        verify(memberService, times(1)).login("test@example.com", "password123");
    }

    @Test
    @DisplayName("로그인 실패 - JSON API")
    void loginJson_Fail() throws Exception {
        when(memberService.login("test@example.com", "wrongpassword"))
                .thenReturn(null);

        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrongpassword");

        mockMvc.perform(post("/api/members/login")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("세션 정보 조회 성공")
    void getSessionMember_Success() throws Exception {
        // given
        // 세션은 MockHttpSession으로 자동 생성됨

        // when & then
        mockMvc.perform(get("/api/members/session")
                .sessionAttr("loginEmail", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loginEmail").value("test@example.com"));
    }

    @Test
    @DisplayName("세션 정보 조회 - 세션 없음")
    void getSessionMember_NoSession() throws Exception {
        // when & then
        mockMvc.perform(get("/api/members/session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loginEmail").isEmpty());
    }
}

