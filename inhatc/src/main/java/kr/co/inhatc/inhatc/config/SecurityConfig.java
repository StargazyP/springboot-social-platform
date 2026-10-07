package kr.co.inhatc.inhatc.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import lombok.extern.slf4j.Slf4j;

/**
 * Spring Security 설정
 * 비밀번호 암호화를 위한 PasswordEncoder Bean 제공
 * 기존 세션 기반 인증 시스템과 호환되도록 설정
 */
@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

    @Value("${app.security.bcrypt.strength:10}")
    private int bcryptStrength;

    /**
     * BCrypt PasswordEncoder Bean 등록
     * 비밀번호 해싱 및 검증에 사용
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(bcryptStrength);
    }

    /**
     * SecurityFilterChain 설정
     * 기존 세션 기반 인증과 호환되도록 모든 요청 허용
     * (실제 인증은 HttpSession으로 처리)
     * 세션 보안 설정 포함
     * Actuator 엔드포인트 보안 설정
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // CSRF 비활성화 (기존 세션 기반 인증과 호환)
            .authorizeHttpRequests(auth -> auth
                // Actuator Health Check 엔드포인트는 공개 (모니터링 도구 접근 허용)
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                // 기타 Actuator 엔드포인트는 인증 필요 (프로덕션에서는 IP 제한 권장)
                .requestMatchers("/actuator/**").permitAll() // 개발 환경: 모든 Actuator 엔드포인트 허용
                .anyRequest().permitAll() // 모든 요청 허용 (기존 인증 시스템 사용)
            )
            .sessionManagement(session -> session
                // 세션 고정 공격 방지: 로그인 시 세션 ID 변경
                .sessionFixation().changeSessionId()
                // 최대 세션 수 제한 (동시 로그인 제한)
                .maximumSessions(1)
                // 세션 만료 시 처리
                .maxSessionsPreventsLogin(false)
            );
        
        return http.build();
    }

    /**
     * 세션 이벤트 리스너 등록
     * 세션 생성/만료 이벤트를 추적하기 위해 필요
     */
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }
}

