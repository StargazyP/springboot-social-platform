package kr.co.inhatc.inhatc;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
import org.springframework.beans.factory.annotation.Value;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import kr.co.inhatc.inhatc.dto.FollowStatsDTO;
import kr.co.inhatc.inhatc.dto.MemberDTO;
import kr.co.inhatc.inhatc.dto.PostResponseDTO;
import kr.co.inhatc.inhatc.service.FollowService;
import kr.co.inhatc.inhatc.service.MemberService;
import kr.co.inhatc.inhatc.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
@Validated
@Slf4j
public class MemberPageController {

    private final MemberService memberService;
    private final PostService postService;
    private final FollowService followService;
    
    @Value("${app.upload.profile-dir}")
    private String profileUploadDir;

    /**
     * 마이페이지 조회
     */
    @GetMapping("/mypage")
    public String mypage(HttpSession session, Model model) {
        String email = (String) session.getAttribute("loginEmail");
        if (email == null) {
            return "redirect:/login";
        }

        MemberDTO member = memberService.getMemberByEmail(email);
        model.addAttribute("loggedInMember", member);
        model.addAttribute("viewedMember", member); // mypage.html 호환성을 위해 추가
        model.addAttribute("loginEmail", email);
        model.addAttribute("viewedEmail", email); // 디버깅을 위해 추가
        Boolean isOwnerValue = Boolean.valueOf(true); // 본인 페이지이므로 true
        model.addAttribute("isOwner", isOwnerValue); // 명시적으로 Boolean 객체로 변환
        
        log.debug("마이페이지 조회: email={}, isOwner={}", email, isOwnerValue);

        // 사용자의 게시글 목록 조회
        List<PostResponseDTO> userPosts = postService.findByMemberEmail(email);
        model.addAttribute("userpost", userPosts);
        model.addAttribute("userPosts", userPosts); // mypage.html 호환성을 위해 추가

        // 팔로우 통계 조회
        FollowStatsDTO followStats = followService.getFollowStats(email, email);
        model.addAttribute("followStats", followStats);

        return "mypage";
    }

    /**
     * 프로필 사진 업로드
     */
    @PostMapping("/upload")
    public String uploadProfile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("loginEmail") @NotBlank(message = "이메일은 필수입니다.") @Email(message = "올바른 이메일 형식이 아닙니다.") String loginEmail,
            HttpSession session,
            Model model) {
        // 세션에서 로그인된 사용자 확인
        String sessionEmail = (String) session.getAttribute("loginEmail");
        
        log.debug("프로필 업로드 요청: sessionEmail={}, loginEmail={}", sessionEmail, loginEmail);
        
        // 보안 검증: 세션의 이메일과 요청한 이메일이 일치해야 함
        if (sessionEmail == null) {
            log.warn("프로필 업로드 실패: 세션에 로그인 정보가 없음");
            model.addAttribute("error", "로그인이 필요합니다.");
            return "redirect:/login";
        }
        
        if (!sessionEmail.equals(loginEmail)) {
            log.warn("프로필 업로드 실패: 다른 사용자의 프로필 수정 시도 - sessionEmail={}, loginEmail={}", 
                sessionEmail, loginEmail);
            model.addAttribute("error", "본인의 프로필만 수정할 수 있습니다.");
            return "redirect:/member/mypage";
        }
        
        try {
            String result = memberService.storeFile(file, loginEmail);
            model.addAttribute("message", result);
            log.info("프로필 업로드 성공: {}", loginEmail);
        } catch (IllegalArgumentException e) {
            log.warn("프로필 업로드 실패 (검증 오류): {}, 사용자={}", e.getMessage(), loginEmail);
            model.addAttribute("error", String.format(kr.co.inhatc.inhatc.constants.AppConstants.ErrorMessage.UPLOAD_FAILED, e.getMessage()));
        } catch (IOException e) {
            log.error("프로필 업로드 중 오류 발생: 사용자={}", loginEmail, e);
            model.addAttribute("error", kr.co.inhatc.inhatc.constants.AppConstants.ErrorMessage.UPLOAD_SERVER_ERROR);
        }
        return "redirect:/member/mypage";
    }

    /**
     * 게시글 삭제
     */
    @PostMapping("/delete")
    public String deletePost(@RequestParam("id") @jakarta.validation.constraints.NotNull(message = "게시글 ID는 필수입니다.") Long postId, HttpSession session) {
        String sessionEmail = (String) session.getAttribute("loginEmail");
        
        log.debug("게시글 삭제 요청: sessionEmail={}, postId={}", sessionEmail, postId);
        
        if (sessionEmail == null) {
            log.warn("게시글 삭제 실패: 세션에 로그인 정보가 없음");
            return "redirect:/login";
        }

        // 게시글 작성자 확인 (보안 강화)
        PostResponseDTO post = postService.findById(postId);
        if (post == null) {
            log.warn("게시글 삭제 실패: 게시글을 찾을 수 없음 - postId={}", postId);
            return "redirect:/member/mypage";
        }
        
        String writerEmail = post.getWriterEmail();
        boolean isOwner = sessionEmail.equals(writerEmail);
        
        if (!isOwner) {
            log.warn("게시글 삭제 실패: 다른 사용자의 게시글 삭제 시도 - sessionEmail={}, writerEmail={}, postId={}", 
                sessionEmail, writerEmail, postId);
            return "redirect:/member/mypage";
        }
        
        postService.delete(postId, sessionEmail);
        log.info("게시글 삭제 성공: postId={}, 사용자={}", postId, sessionEmail);
        return "redirect:/member/mypage";
    }

    /**
     * 메인 페이지 (게시글 목록)
     */
    @GetMapping("/view")
    public String view(HttpSession session, Model model) {
        String email = (String) session.getAttribute("loginEmail");
        if (email == null) {
            return "redirect:/login";
        }
        return "redirect:/main";
    }

    /**
     * 프로필 이미지 조회 (WebConfig 매핑을 통한 직접 접근 대신 fallback으로 사용)
     * 주로 mypage.html에서 사용하는 엔드포인트
     * 실제로는 /static/{email}/profile.png로 직접 접근 가능하지만,
     * 다양한 확장자 지원 및 기본 이미지 처리를 위해 유지
     */
    @GetMapping("/Userimgsource/{email}")
    public ResponseEntity<Resource> getUserImage(@PathVariable String email) throws MalformedURLException {
        try {
            // 설정에서 가져온 프로필 이미지 저장 경로
            Path file = null;
            
            // 확장자별로 시도 (png, jpg, jpeg)
            String[] extensions = {"png", "jpg", "jpeg"};
            for (String ext : extensions) {
                Path testPath = Paths.get(profileUploadDir, email, "profile." + ext);
                if (Files.exists(testPath)) {
                    file = testPath;
                    break;
                }
            }
            
            // 프로필 이미지가 없으면 기본 이미지 사용
            if (file == null) {
                file = Paths.get(profileUploadDir, "images", "default-profile.png");
                if (!Files.exists(file)) {
                    return ResponseEntity.notFound().build();
                }
            }

            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            // 파일 확장자에 따라 Content-Type 결정
            String contentType = "image/png";
            String filename = file.getFileName().toString().toLowerCase();
            if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
                contentType = "image/jpeg";
            } else if (filename.endsWith(".gif")) {
                contentType = "image/gif";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFileName() + "\"")
                    .body(resource);
        } catch (Exception e) {
            log.error("프로필 이미지 조회 중 오류 발생: email={}", email, e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 알림/게시글 확인 페이지 (임시로 메인 페이지로 리다이렉트)
     */
    @GetMapping("/PostCheck")
    public String postCheck(HttpSession session) {
        String email = (String) session.getAttribute("loginEmail");
        if (email == null) {
            return "redirect:/login";
        }
        return "redirect:/main";
    }

    /**
     * 다른 사용자 프로필 조회 (이메일로) - mypage.html 재활용
     */
    @GetMapping("/profile/{email}")
    public String viewUserProfile(@PathVariable String email, HttpSession session, Model model) {
        // 로그인 확인
        String loginEmail = (String) session.getAttribute("loginEmail");
        if (loginEmail == null) {
            return "redirect:/login";
        }

        try {
            // URL 디코딩 (이메일 주소에 @ 등 특수문자가 포함될 수 있음)
            String decodedEmail = URLDecoder.decode(email, "UTF-8");
            
            // 조회할 사용자 정보 가져오기
            MemberDTO viewedMember = memberService.getMemberByEmail(decodedEmail);
            model.addAttribute("viewedMember", viewedMember);
            model.addAttribute("loggedInMember", viewedMember); // mypage.html 호환성을 위해 추가

            // 세션 유저와 조회 중인 유저가 같은지 확인
            boolean isOwnerPrimitive = loginEmail != null && loginEmail.equals(decodedEmail);
            Boolean isOwnerValue = Boolean.valueOf(isOwnerPrimitive); // 명시적으로 Boolean 객체로 변환
            model.addAttribute("isOwner", isOwnerValue);
            model.addAttribute("loginEmail", loginEmail);
            model.addAttribute("viewedEmail", decodedEmail); // 디버깅을 위해 추가
            
            log.debug("사용자 프로필 조회: loginEmail={}, viewedEmail={}, isOwner={}", 
                loginEmail, decodedEmail, isOwnerValue);

            // 해당 사용자의 게시글 목록 조회
            List<PostResponseDTO> userPosts = postService.findByMemberEmail(decodedEmail);
            model.addAttribute("userPosts", userPosts);
            model.addAttribute("userpost", userPosts); // mypage.html 호환성을 위해 추가

            // 팔로우 통계 조회
            FollowStatsDTO followStats = followService.getFollowStats(decodedEmail, loginEmail);
            model.addAttribute("followStats", followStats);

            return "mypage";
        } catch (Exception e) {
            log.error("사용자 프로필 조회 중 오류 발생: email={}", email, e);
            // 사용자를 찾을 수 없으면 메인 페이지로 리다이렉트
            return "redirect:/main";
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
            session.invalidate();
            log.info("로그아웃: 세션 무효화 완료");
        }
        return "redirect:/login";
    }
}

