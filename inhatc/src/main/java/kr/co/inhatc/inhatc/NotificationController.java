package kr.co.inhatc.inhatc;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession;
import kr.co.inhatc.inhatc.dto.NotificationDTO;
import kr.co.inhatc.inhatc.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 알림 확인 페이지 (HTML) - 읽지 않은 알림만
     */
    @GetMapping("/posts/check")
    public String checkNotifications(HttpSession session, Model model) {
        String loginEmail = (String) session.getAttribute("loginEmail");
        if (loginEmail == null) {
            return "redirect:/login";
        }

        try {
            List<NotificationDTO> notifications = notificationService.getUnreadNotifications(loginEmail);
            model.addAttribute("notifications", notifications);
            model.addAttribute("loginEmail", loginEmail);
        } catch (Exception e) {
            log.error("알림 조회 중 오류 발생: loginEmail={}", loginEmail, e);
            // 에러 발생 시 빈 리스트로 처리
            model.addAttribute("notifications", java.util.Collections.emptyList());
            model.addAttribute("loginEmail", loginEmail);
            model.addAttribute("error", "알림을 불러오는 중 오류가 발생했습니다.");
        }

        return "notification"; // notification.html 템플릿 사용
    }

    /**
     * 알림 페이지 (HTML) - 모든 알림
     */
    @GetMapping("/posts/notifications/page")
    public String notificationsPage(HttpSession session, Model model) {
        String loginEmail = (String) session.getAttribute("loginEmail");
        if (loginEmail == null) {
            return "redirect:/login";
        }

        try {
            List<NotificationDTO> notifications = notificationService.getAllNotifications(loginEmail);
            model.addAttribute("notifications", notifications);
            model.addAttribute("loginEmail", loginEmail);
        } catch (Exception e) {
            log.error("알림 조회 중 오류 발생: loginEmail={}", loginEmail, e);
            // 에러 발생 시 빈 리스트로 처리
            model.addAttribute("notifications", java.util.Collections.emptyList());
            model.addAttribute("loginEmail", loginEmail);
            model.addAttribute("error", "알림을 불러오는 중 오류가 발생했습니다.");
        }

        return "notifications"; // notifications.html 템플릿 사용
    }

    /**
     * 알림 조회 API (JSON) - 읽지 않은 알림만
     */
    // REST 개선: JSON 알림 API는 /api/notifications 아래로 분리한다.
    @GetMapping({"/api/notifications", "/posts/notifications"})
    @ResponseBody
    public ResponseEntity<List<NotificationDTO>> getNotifications(HttpSession session) {
        String loginEmail = (String) session.getAttribute("loginEmail");
        if (loginEmail == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            List<NotificationDTO> notifications = notificationService.getUnreadNotifications(loginEmail);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            log.error("알림 조회 API 오류 발생: loginEmail={}", loginEmail, e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 모든 알림 조회 API (JSON) - 읽음/안읽음 모두
     */
    @GetMapping({"/api/notifications/all", "/posts/notifications/all"})
    @ResponseBody
    public ResponseEntity<List<NotificationDTO>> getAllNotifications(HttpSession session) {
        String loginEmail = (String) session.getAttribute("loginEmail");
        if (loginEmail == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            List<NotificationDTO> notifications = notificationService.getAllNotifications(loginEmail);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            log.error("전체 알림 조회 API 오류 발생: loginEmail={}", loginEmail, e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 알림 읽음 처리 API
     */
    @PostMapping({"/api/notifications/{notificationId}/read", "/posts/notifications/read"})
    @ResponseBody
    public ResponseEntity<String> markNotificationAsRead(
            @org.springframework.web.bind.annotation.PathVariable(required = false) Long notificationId,
            @RequestParam(required = false) Long notificationIdParam,
            HttpSession session) {
        Long targetNotificationId = notificationId != null ? notificationId : notificationIdParam;
        String loginEmail = (String) session.getAttribute("loginEmail");
        if (loginEmail == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }
        if (targetNotificationId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("notificationId가 필요합니다.");
        }

        try {
            notificationService.markAsRead(targetNotificationId);
            log.debug("알림 읽음 처리: notificationId={}, loginEmail={}", targetNotificationId, loginEmail);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("알림 읽음 처리 실패: notificationId={}, loginEmail={}", targetNotificationId, loginEmail, e);
            return ResponseEntity.status(500).body("알림 읽음 처리 실패: 서버 오류가 발생했습니다.");
        }
    }

    /**
     * 모든 알림 읽음 처리 API
     */
    @PostMapping({"/api/notifications/read-all", "/posts/notifications/read-all"})
    @ResponseBody
    public ResponseEntity<String> markAllNotificationsAsRead(HttpSession session) {
        String loginEmail = (String) session.getAttribute("loginEmail");
        if (loginEmail == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        try {
            notificationService.markAllAsRead(loginEmail);
            log.info("모든 알림 읽음 처리: loginEmail={}", loginEmail);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("모든 알림 읽음 처리 실패: loginEmail={}", loginEmail, e);
            return ResponseEntity.status(500).body("알림 읽음 처리 실패: 서버 오류가 발생했습니다.");
        }
    }
}

