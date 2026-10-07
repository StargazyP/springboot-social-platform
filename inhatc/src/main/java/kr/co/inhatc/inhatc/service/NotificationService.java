package kr.co.inhatc.inhatc.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import kr.co.inhatc.inhatc.dto.NotificationDTO;
import kr.co.inhatc.inhatc.entity.MemberEntity;
import kr.co.inhatc.inhatc.entity.NotificationEntity;
import kr.co.inhatc.inhatc.entity.PostEntity;
import kr.co.inhatc.inhatc.repository.MemberRepository;
import kr.co.inhatc.inhatc.repository.NotificationRepository;
import kr.co.inhatc.inhatc.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final SimpMessagingTemplate messagingTemplate; // 12-23 WebSocket 실시간 알림 전송을 위해 추가

    /**
     * 알림 생성 (좋아요)
     */
    public void createLikeNotification(Long postId, String actorEmail) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        // 본인 게시물에 좋아요를 누른 경우 알림 생성하지 않음
        if (post.getMemberEmail().equals(actorEmail)) {
            return;
        }

        // 이미 같은 사용자가 같은 게시물에 좋아요 알림을 보낸 경우 중복 방지
        boolean exists = notificationRepository.findByPostId(postId).stream()
                .anyMatch(n -> n.getActorEmail().equals(actorEmail) 
                        && n.getNotificationType().equals("LIKE")
                        && !n.getIsRead());

        if (!exists) {
            NotificationEntity notification = NotificationEntity.builder()
                    .post(post)
                    .notificationType("LIKE")
                    .actorEmail(actorEmail)
                    .recipientEmail(post.getMemberEmail())
                    .build();

            NotificationEntity savedNotification = notificationRepository.save(notification);
            
            // 12-23 실시간 알림 전송 (WebSocket)
            sendNotificationToUser(post.getMemberEmail(), savedNotification);
        }
    }

    /**
     * 알림 생성 (댓글)
     */
    public void createCommentNotification(Long postId, String actorEmail) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        // 본인 게시물에 댓글을 남긴 경우 알림 생성하지 않음
        if (post.getMemberEmail().equals(actorEmail)) {
            return;
        }

        NotificationEntity notification = NotificationEntity.builder()
                .post(post)
                .notificationType("COMMENT")
                .actorEmail(actorEmail)
                .recipientEmail(post.getMemberEmail())
                .build();
        
        NotificationEntity savedNotification = notificationRepository.save(notification);
        
        // 12-23 실시간 알림 전송 (WebSocket)
        sendNotificationToUser(post.getMemberEmail(), savedNotification);
    }

    /**
     * 읽지 않은 알림 수 조회
     */
    public long getUnreadCount(String recipientEmail) {
        return notificationRepository.countByRecipientEmailAndIsReadFalse(recipientEmail);
    }

    /**
     * 사용자의 읽지 않은 알림 조회
     */
    public List<NotificationDTO> getUnreadNotifications(String recipientEmail) {
        List<NotificationEntity> notifications = notificationRepository
                .findByRecipientEmailAndIsReadFalseOrderByCreatedAtDesc(recipientEmail);

        return notifications.stream()
                .map(notification -> {
                    NotificationDTO dto = NotificationDTO.fromEntity(notification);
                    // actorName 설정
                    memberRepository.findByMemberEmail(notification.getActorEmail())
                            .ifPresent(member -> dto.setActorName(member.getMemberName()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * 사용자의 모든 알림 조회 (읽음/안읽음 모두)
     */
    public List<NotificationDTO> getAllNotifications(String recipientEmail) {
        List<NotificationEntity> notifications = notificationRepository
                .findByRecipientEmailOrderByCreatedAtDesc(recipientEmail);

        return notifications.stream()
                .map(notification -> {
                    NotificationDTO dto = NotificationDTO.fromEntity(notification);
                    // actorName 설정
                    memberRepository.findByMemberEmail(notification.getActorEmail())
                            .ifPresent(member -> dto.setActorName(member.getMemberName()));
                    return dto;
                })
                .collect(Collectors.toList());
    }


    /**
     * 모든 알림 읽음 처리
     */
    public void markAllAsRead(String recipientEmail) {
        List<NotificationEntity> notifications = notificationRepository
                .findByRecipientEmailAndIsReadFalseOrderByCreatedAtDesc(recipientEmail);
        notifications.forEach(NotificationEntity::markAsRead);
        notificationRepository.saveAll(notifications);
        
        // 12-23 실시간으로 알림 수 업데이트 전송
        sendNotificationCount(recipientEmail);
    }
    
    /**
     * WebSocket을 통해 특정 사용자에게 알림 전송
     * 12-23 실시간 알림 기능 추가
     */
    private void sendNotificationToUser(String recipientEmail, NotificationEntity notification) {
        try {
            // NotificationDTO로 변환
            NotificationDTO dto = NotificationDTO.fromEntity(notification);
            
            // actorName 설정
            memberRepository.findByMemberEmail(notification.getActorEmail())
                    .ifPresent(member -> dto.setActorName(member.getMemberName()));
            
            // WebSocket으로 알림 전송
            // topic 경로는 사용자 이메일을 기반으로 함 (URL 인코딩 필요)
            String topic = "/topic/notifications/" + recipientEmail.replace("@", "_").replace(".", "_");
            messagingTemplate.convertAndSend(topic, dto);
            
            log.debug("알림 전송 완료: recipient={}, type={}", recipientEmail, notification.getNotificationType());
            
            // 알림 수 업데이트 전송
            sendNotificationCount(recipientEmail);
        } catch (Exception e) {
            log.error("알림 전송 실패: recipient={}", recipientEmail, e);
        }
    }
    
    /**
     * WebSocket을 통해 알림 수 전송
     * 12-23 실시간 알림 수 업데이트 기능 추가
     */
    private void sendNotificationCount(String recipientEmail) {
        try {
            long unreadCount = notificationRepository
                    .countByRecipientEmailAndIsReadFalse(recipientEmail);
            
            String topic = "/topic/notifications/count/" + recipientEmail.replace("@", "_").replace(".", "_");
            messagingTemplate.convertAndSend(topic, unreadCount);
            
            log.debug("알림 수 전송 완료: recipient={}, count={}", recipientEmail, unreadCount);
        } catch (Exception e) {
            log.error("알림 수 전송 실패: recipient={}", recipientEmail, e);
        }
    }
    
    /**
     * 알림 읽음 처리 시 알림 수 업데이트
     * 12-23 WebSocket을 통한 실시간 알림 수 업데이트 추가
     */
    public void markAsRead(Long notificationId) {
        NotificationEntity notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("알림을 찾을 수 없습니다."));
        notification.markAsRead();
        notificationRepository.save(notification);
        
        // 12-23 실시간으로 알림 수 업데이트 전송
        sendNotificationCount(notification.getRecipientEmail());
    }
}

