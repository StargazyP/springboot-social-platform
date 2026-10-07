package kr.co.inhatc.inhatc.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import kr.co.inhatc.inhatc.dto.MemberDTO;
import kr.co.inhatc.inhatc.entity.MemberEntity;
import kr.co.inhatc.inhatc.repository.MemberRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberService 단위 테스트")
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberService memberService;

    private MemberEntity testMember;
    private String testEmail = "test@example.com";
    private String testPassword = "password123";
    private String encodedPassword = "$2a$10$encodedPasswordHash";

    @BeforeEach
    void setUp() {
        testMember = MemberEntity.builder()
                .id(1L)
                .memberEmail(testEmail)
                .memberName("테스트 사용자")
                .memberPassword(encodedPassword)
                .build();

        // @Value 필드 주입
        ReflectionTestUtils.setField(memberService, "profileUploadDir", "/test/uploads/profiles");
    }

    @Test
    @DisplayName("회원가입 성공")
    void save_Success() {
        MemberDTO dto = MemberDTO.builder()
                .memberEmail(testEmail)
                .memberPassword(testPassword)
                .memberName("테스트 사용자")
                .build();

        when(memberRepository.findByMemberEmail(testEmail)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(testPassword)).thenReturn(encodedPassword);
        when(memberRepository.save(any(MemberEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        memberService.save(dto);

        verify(memberRepository, times(1)).findByMemberEmail(testEmail);
        verify(passwordEncoder, times(1)).encode(testPassword);
        verify(memberRepository, times(1)).save(any(MemberEntity.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 중복 이메일")
    void save_Fail_DuplicateEmail() {
        MemberDTO dto = MemberDTO.builder()
                .memberEmail(testEmail)
                .memberPassword(testPassword)
                .memberName("테스트 사용자")
                .build();

        when(memberRepository.findByMemberEmail(testEmail)).thenReturn(Optional.of(testMember));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> memberService.save(dto));
        assertTrue(ex.getMessage().contains("이미 사용 중인 이메일"));
        verify(memberRepository, never()).save(any());
    }

    @Test
    @DisplayName("로그인 성공 - 암호화된 비밀번호")
    void login_Success_WithEncryptedPassword() {
        // given
        when(memberRepository.findByMemberEmail(testEmail))
                .thenReturn(Optional.of(testMember));
        when(passwordEncoder.matches(testPassword, encodedPassword))
                .thenReturn(true);

        // when
        MemberDTO result = memberService.login(testEmail, testPassword);

        // then
        assertNotNull(result);
        assertEquals(testEmail, result.getMemberEmail());
        verify(memberRepository, times(1)).findByMemberEmail(testEmail);
        verify(passwordEncoder, times(1)).matches(testPassword, encodedPassword);
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 이메일")
    void login_Fail_EmailNotFound() {
        // given
        when(memberRepository.findByMemberEmail(testEmail))
                .thenReturn(Optional.empty());

        // when
        MemberDTO result = memberService.login(testEmail, testPassword);

        // then
        assertNull(result);
        verify(memberRepository, times(1)).findByMemberEmail(testEmail);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void login_Fail_WrongPassword() {
        // given
        when(memberRepository.findByMemberEmail(testEmail))
                .thenReturn(Optional.of(testMember));
        when(passwordEncoder.matches(testPassword, encodedPassword))
                .thenReturn(false);

        // when
        MemberDTO result = memberService.login(testEmail, testPassword);

        // then
        assertNull(result);
        verify(memberRepository, times(1)).findByMemberEmail(testEmail);
        verify(passwordEncoder, times(1)).matches(testPassword, encodedPassword);
    }

    @Test
    @DisplayName("이메일로 회원 조회 성공")
    void getMemberByEmail_Success() {
        // given
        when(memberRepository.findByMemberEmail(testEmail))
                .thenReturn(Optional.of(testMember));

        // when
        MemberDTO result = memberService.getMemberByEmail(testEmail);

        // then
        assertNotNull(result);
        assertEquals(testEmail, result.getMemberEmail());
        verify(memberRepository, times(1)).findByMemberEmail(testEmail);
    }

    @Test
    @DisplayName("이메일로 회원 조회 실패 - 존재하지 않는 회원")
    void getMemberByEmail_NotFound() {
        // given
        when(memberRepository.findByMemberEmail(testEmail))
                .thenReturn(Optional.empty());

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            memberService.getMemberByEmail(testEmail);
        });

        assertTrue(exception.getMessage().contains("회원이 존재하지 않습니다"));
        verify(memberRepository, times(1)).findByMemberEmail(testEmail);
    }
}

