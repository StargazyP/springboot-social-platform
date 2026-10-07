package kr.co.inhatc.inhatc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import kr.co.inhatc.inhatc.entity.NotificationEntity;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    
    // 특정 사용자의 읽지 않은 알림 조회 (최신순)
    // ✅ 안전: JPA 메서드 이름 기반 쿼리 (SQL Injection 위험 없음)
    List<NotificationEntity> findByRecipientEmailAndIsReadFalseOrderByCreatedAtDesc(String recipientEmail);
    
    // 특정 사용자의 모든 알림 조회 (최신순)
    // ✅ 안전: JPA 메서드 이름 기반 쿼리 (SQL Injection 위험 없음)
    List<NotificationEntity> findByRecipientEmailOrderByCreatedAtDesc(String recipientEmail);
    
    // 특정 게시물의 알림 조회
    // ✅ 안전: JPA 메서드 이름 기반 쿼리 (SQL Injection 위험 없음)
    List<NotificationEntity> findByPostId(Long postId);

    long countByRecipientEmailAndIsReadFalse(String recipientEmail);
}

