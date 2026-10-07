package kr.co.inhatc.inhatc.config;

import javax.sql.DataSource;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import kr.co.inhatc.inhatc.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JVM·DB·BCrypt 경로를 기동 직후 예열하여 첫 로그인 cold start를 줄인다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AppWarmupRunner implements ApplicationRunner {

    private final DataSource dataSource;
    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        long started = System.currentTimeMillis();

        try (var connection = dataSource.getConnection()) {
            connection.isValid(2);
        }

        String dummyHash = passwordEncoder.encode("__warmup__");
        passwordEncoder.matches("__warmup__", dummyHash);

        memberRepository.count();

        log.info("애플리케이션 warmup 완료 ({}ms)", System.currentTimeMillis() - started);
    }
}
