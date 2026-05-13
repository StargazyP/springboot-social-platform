package kr.co.inhatc.inhatc;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.validation.annotation.Validated;

import jakarta.servlet.http.HttpSession;
import kr.co.inhatc.inhatc.dto.ConversationDTO;
import kr.co.inhatc.inhatc.dto.MessageDTO;
import kr.co.inhatc.inhatc.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Slf4j
@Validated
public class MessageController {
    
    @Value("${app.upload.messages-dir}")
    private String messagesUploadDir;

    private final MessageService messageService;

    // REST 개선: 메시지 목록은 컬렉션 조회로 표현하고 발신자는 세션에서만 신뢰한다.
    @GetMapping(params = "receiver")
    public ResponseEntity<List<MessageDTO>> getMessages(
            @RequestParam("receiver") String receiver,
            HttpSession session) {
        String sender = (String) session.getAttribute("loginEmail");
        if (sender == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return getMessageHistory(sender, receiver, session);
    }

    @GetMapping("/history")
    public ResponseEntity<List<MessageDTO>> getMessageHistory(
            @RequestParam("sender") String sender,
            @RequestParam("receiver") String receiver,
            HttpSession session) {
        log.debug("메시지 히스토리 조회 요청: sender={}, receiver={}", sender, receiver);
        
        try {
            String loginEmail = (String) session.getAttribute("loginEmail");
            if (loginEmail == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            if (!loginEmail.equals(sender)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            if (sender == null || sender.trim().isEmpty()) {
                log.warn("메시지 히스토리 조회 실패: sender가 비어있음");
                return ResponseEntity.badRequest().build();
            }
            
            if (receiver == null || receiver.trim().isEmpty()) {
                log.warn("메시지 히스토리 조회 실패: receiver가 비어있음");
                return ResponseEntity.badRequest().build();
            }
            
            List<MessageDTO> messages = messageService.getMessageHistory(sender, receiver);
            log.debug("메시지 히스토리 조회 완료: {}개", messages.size());
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            log.error("메시지 히스토리 조회 중 오류 발생: sender={}, receiver={}", sender, receiver, e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationDTO>> getConversations(
            @RequestParam(value = "userEmail", required = false) String userEmail,
            HttpSession session) {
        String loginEmail = (String) session.getAttribute("loginEmail");
        if (loginEmail == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String requestedUserEmail = userEmail == null ? loginEmail : userEmail;
        if (!loginEmail.equals(requestedUserEmail)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        log.debug("대화 상대 목록 조회 요청: userEmail={}", userEmail);
        
        try {
            if (requestedUserEmail.trim().isEmpty()) {
                log.warn("대화 상대 목록 조회 실패: userEmail이 비어있음");
                return ResponseEntity.badRequest().build();
            }
            
            List<ConversationDTO> conversations = messageService.getConversations(requestedUserEmail);
            log.debug("대화 상대 목록 조회 완료: {}개", conversations.size());
            return ResponseEntity.ok(conversations);
        } catch (Exception e) {
            log.error("대화 상대 목록 조회 중 오류 발생: userEmail={}", requestedUserEmail, e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 메시지 이미지 업로드
     * 저장 경로: C:/Users/jdajs/spring test/inhatc/src/main/resources/static/{userEmail}/messages/{timestamp}.{ext}
     * DB 저장 경로: /images/{userEmail}/messages/{filename}
     */
    // REST 개선: 이미지 업로드도 메시지의 하위 컬렉션(/images)으로 표현한다.
    @PostMapping({"/images", "/upload-image"})
    public ResponseEntity<Map<String, String>> uploadMessageImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userEmail") @jakarta.validation.constraints.NotBlank(message = "이메일은 필수입니다.") 
            @jakarta.validation.constraints.Email(message = "올바른 이메일 형식이 아닙니다.") String userEmail,
            HttpSession session) {
        log.debug("MessageController.uploadMessageImage 호출됨 - userEmail: {}, 파일명: {}", 
            userEmail, file != null ? file.getOriginalFilename() : "null");
        
        Map<String, String> response = new HashMap<>();
        
        try {
            String loginEmail = (String) session.getAttribute("loginEmail");
            if (loginEmail == null) {
                response.put("error", "로그인이 필요합니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            if (!loginEmail.equals(userEmail)) {
                response.put("error", "본인의 메시지 이미지만 업로드할 수 있습니다.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            if (file == null || file.isEmpty()) {
                log.warn("파일이 비어있음");
                response.put("error", kr.co.inhatc.inhatc.constants.AppConstants.ErrorMessage.FILE_EMPTY);
                return ResponseEntity.badRequest().body(response);
            }
            
            // 공통 유틸리티 사용으로 중복 코드 제거
            String imagePath = kr.co.inhatc.inhatc.util.FileUploadService.uploadMessageImage(file, userEmail, messagesUploadDir);
            
            response.put("imagePath", imagePath);
            response.put("message", kr.co.inhatc.inhatc.constants.AppConstants.SuccessMessage.IMAGE_UPLOAD_SUCCESS);
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            log.error("이미지 업로드 중 오류 발생: userEmail={}", userEmail, e);
            response.put("error", kr.co.inhatc.inhatc.constants.AppConstants.ErrorMessage.IMAGE_UPLOAD_FAILED);
            return ResponseEntity.status(500).body(response);
        }
    }
}

