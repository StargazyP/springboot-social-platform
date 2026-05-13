package kr.co.inhatc.inhatc;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import kr.co.inhatc.inhatc.dto.MemberDTO;
import kr.co.inhatc.inhatc.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Slf4j
@Validated
public class MemberController {

    private final MemberService memberService;

    /**
     * 회원가입
     */
    // @PostMapping("/register")
    // public ResponseEntity<String> register(@RequestBody MemberDTO memberDTO) {
    // memberService.save(memberDTO);
    // return ResponseEntity.status(HttpStatus.CREATED).body("회원가입이 완료되었습니다.");
    // }

    /**
     * 로그인
     * 세션 고정 공격 방지: 로그인 성공 시 기존 세션 무효화 후 새 세션 생성
     */
    @PostMapping("/login")
    public String login(
            @RequestParam @NotBlank(message = "이메일은 필수입니다.") @Email(message = "올바른 이메일 형식이 아닙니다.") String email,
            @RequestParam @NotBlank(message = "비밀번호는 필수입니다.") String password,
            HttpServletRequest request,
            Model model) {

        MemberDTO memberDTO = memberService.login(email, password);

        if (memberDTO != null) {
            // 세션 고정 공격 방지: 기존 세션 무효화
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }
            // 새 세션 생성
            HttpSession newSession = request.getSession(true);
            // 세션에 이메일 저장
            newSession.setAttribute("loginEmail", email);
            log.info("로그인 성공: {} (세션 ID 변경됨)", email);

            return "redirect:/main";
        } else {
            // 로그인 실패 시 에러 메시지 전달 후 로그인 페이지로
            log.warn("로그인 실패: {}", email);
            model.addAttribute("error", "이메일 또는 비밀번호가 올바르지 않습니다.");
            return "login"; // login.html
        }
    }

    /**
     * 로그아웃
     * 세션 무효화 처리
     */
    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate(); // 세션 종료
            log.info("로그아웃: 세션 무효화 완료");
        }
        return "redirect:/login";
    }

    /**
     * 현재 로그인된 사용자 정보 가져오기
     */
    @GetMapping("/session")
    public ResponseEntity<Map<String, String>> getSessionMember(HttpSession session) {
        Map<String, String> response = new HashMap<>();
        if (session == null) {
            response.put("loginEmail", null);
            return ResponseEntity.ok(response);
        }
        String loginEmail = (String) session.getAttribute("loginEmail");
        response.put("loginEmail", loginEmail);
        return ResponseEntity.ok(response);
    }

    /**
     * 마이페이지 (이메일 기반 조회)
     */
    @GetMapping("/{email}/mypage")
    public ResponseEntity<MemberDTO> getMyPage(@PathVariable String email) {
        return ResponseEntity.ok(memberService.getMemberByEmail(email));
    }

    /**
     * 프로필 사진 업로드
     */
    // REST 개선: 프로필 이미지를 멤버의 하위 리소스로 표현한다.
    @PostMapping({"/{email}/profile-image", "/{email}/upload-profile"})
    public ResponseEntity<String> uploadProfile(
            @PathVariable @NotBlank(message = "이메일은 필수입니다.") @Email(message = "올바른 이메일 형식이 아닙니다.") String email,
            @RequestParam("file") MultipartFile file) {
        try {
            String result = memberService.storeFile(file, email);
            log.info(kr.co.inhatc.inhatc.constants.AppConstants.SuccessMessage.PROFILE_UPLOAD_SUCCESS + ": {}", email);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            log.warn(String.format(kr.co.inhatc.inhatc.constants.AppConstants.ErrorMessage.PROFILE_UPLOAD_FAILED, e.getMessage()) + ", 사용자={}", email);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(String.format(kr.co.inhatc.inhatc.constants.AppConstants.ErrorMessage.UPLOAD_FAILED, e.getMessage()));
        } catch (IOException e) {
            log.error("프로필 이미지 업로드 중 오류 발생: 사용자={}", email, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(kr.co.inhatc.inhatc.constants.AppConstants.ErrorMessage.UPLOAD_SERVER_ERROR);
        }
    }
}
