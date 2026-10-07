package kr.co.inhatc.inhatc;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import kr.co.inhatc.inhatc.dto.LoginRequest;
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
     * 회원가입 페이지
     */
    @GetMapping("/signup")
    public String signupForm() {
        return "signup";
    }

    /**
     * 회원가입 (HTML 폼)
     */
    @PostMapping(value = "/signup", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String signup(
            @RequestParam @NotBlank(message = "이메일은 필수입니다.") @Email(message = "올바른 이메일 형식이 아닙니다.") String email,
            @RequestParam @NotBlank(message = "비밀번호는 필수입니다.") @Size(min = 4, max = 100, message = "비밀번호는 4자 이상 100자 이하여야 합니다.") String password,
            @RequestParam @NotBlank(message = "이름은 필수입니다.") @Size(max = 50, message = "이름은 50자 이하여야 합니다.") String name,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            MemberDTO memberDTO = MemberDTO.builder()
                    .memberEmail(email)
                    .memberPassword(password)
                    .memberName(name)
                    .build();
            memberService.save(memberDTO);
            redirectAttributes.addFlashAttribute("success", "회원가입이 완료되었습니다. 로그인해 주세요.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            log.warn("회원가입 실패: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "signup";
        }
    }

    /**
     * 회원가입 (JSON API)
     */
    @PostMapping(value = "/signup", consumes = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, String>> signupApi(@Valid @RequestBody MemberDTO memberDTO) {
        try {
            memberService.save(memberDTO);
            Map<String, String> body = new HashMap<>();
            body.put("message", "회원가입이 완료되었습니다.");
            body.put("memberEmail", memberDTO.getMemberEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(body);
        } catch (IllegalArgumentException e) {
            Map<String, String> body = new HashMap<>();
            body.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
        }
    }

    /**
     * 로그인 (HTML 폼 — 레거시 호환)
     */
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String loginForm(
            @RequestParam @NotBlank(message = "이메일은 필수입니다.") @Email(message = "올바른 이메일 형식이 아닙니다.") String email,
            @RequestParam @NotBlank(message = "비밀번호는 필수입니다.") String password,
            HttpServletRequest request,
            Model model) {

        MemberDTO memberDTO = memberService.login(email, password);

        if (memberDTO != null) {
            establishLoginSession(request, email);
            return "redirect:/main";
        }

        log.warn("로그인 실패: {}", email);
        model.addAttribute("error", "이메일 또는 비밀번호가 올바르지 않습니다.");
        return "login";
    }

    /**
     * 로그인 (JSON — fetch 기반, 서버 리다이렉트 없음)
     */
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, String>> loginJson(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {

        MemberDTO memberDTO = memberService.login(loginRequest.getEmail(), loginRequest.getPassword());

        if (memberDTO == null) {
            log.warn("로그인 실패: {}", loginRequest.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "이메일 또는 비밀번호가 올바르지 않습니다."));
        }

        establishLoginSession(request, loginRequest.getEmail());
        Map<String, String> body = new HashMap<>();
        body.put("loginEmail", loginRequest.getEmail());
        body.put("redirect", "/main");
        return ResponseEntity.ok(body);
    }

    private void establishLoginSession(HttpServletRequest request, String email) {
        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }
        HttpSession newSession = request.getSession(true);
        newSession.setAttribute("loginEmail", email);
        log.info("로그인 성공: {} (세션 ID 변경됨)", email);
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
