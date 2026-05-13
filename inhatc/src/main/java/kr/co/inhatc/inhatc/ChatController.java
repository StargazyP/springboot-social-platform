package kr.co.inhatc.inhatc;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import kr.co.inhatc.inhatc.service.MemberService;
import kr.co.inhatc.inhatc.dto.MemberDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final MemberService memberService;

    /**
     * 메시지 페이지
     * - receiverEmail이 없으면: 세션 유저의 message_entity를 조회하여 대화 상대 목록 표시 목록 모드
     * - receiverEmail이 있으면: 세션 유저(senderEmail)와 receiverEmail 간의 채팅 화면 표시 채팅 모드
     * 
     * @param receiverEmail 수신자 이메일 (선택적, 없으면 목록 모드)
     * @param session HTTP 세션 (세션에서 loginEmail을 가져와 senderEmail로 사용)
     * @param model 뷰 모델
     * @return message.html 템플릿
     */
    @GetMapping("/message")
    public String chatPage(
            @RequestParam(value = "receiverEmail", required = false) String receiverEmail,
            HttpSession session,
            Model model) {
        log.debug("채팅 페이지 요청: receiverEmail={}", receiverEmail);
        
        // 세션에서 로그인한 유저의 이메일을 가져옴 (이것이 senderEmail이 됨)
        String loginEmail = (String) session.getAttribute("loginEmail");
        
        if (loginEmail == null) {
            log.warn("채팅 페이지 접근 실패: 세션에 loginEmail이 없음");
            return "redirect:/login";
        }

        // senderEmail은 항상 세션 유저 (로그인한 유저)
        model.addAttribute("senderEmail", loginEmail);
        
        // receiverEmail이 있으면 채팅 모드, 없으면 목록 모드
        if (receiverEmail != null && !receiverEmail.trim().isEmpty()) {
            log.debug("채팅 모드: sender={}, receiver={}", loginEmail, receiverEmail);
            model.addAttribute("receiverEmail", receiverEmail);
            model.addAttribute("isChatMode", true);
            
            // 수신자 이름 조회
            try {
                MemberDTO receiver = memberService.getMemberByEmail(receiverEmail);
                model.addAttribute("receiverName", receiver.getMemberName());
            } catch (Exception e) {
                log.warn("수신자 정보 조회 실패: receiverEmail={}", receiverEmail, e);
                model.addAttribute("receiverName", receiverEmail);
            }
        } else {
            log.debug("목록 모드: sender={}", loginEmail);
            model.addAttribute("isChatMode", false);
            // 대화 상대 목록은 JavaScript에서 /api/messages/conversations?userEmail={senderEmail} API로 가져옴
        }
        
        return "message";
    }
}


