package kr.co.inhatc.inhatc;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 엔드포인트 등록
        registry.addEndpoint("/ws", "/sns/ws")
                .setAllowedOriginPatterns("*") // allowedOrigins 대신 allowedOriginPatterns 사용
                .withSockJS(); // SockJS를 통한 Fallback 지원
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 메시지 브로커 설정
        config.enableSimpleBroker("/topic", "/queue"); // 구독 경로
        config.setApplicationDestinationPrefixes("/app"); // 메시지 전송 경로
    }
}
