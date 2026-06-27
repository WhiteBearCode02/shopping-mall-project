package com.example.shoppingmall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.example.shoppingmall")
@EnableJpaRepositories(basePackages = "com.example.shoppingmall")
@EntityScan(basePackages = "com.example.shoppingmall")
// [스캔 체인 동기화] 패키지 계층 구조 이관에 따른 컴포넌트 및 리포지토리 탐색 주소를 명시적으로 바인딩합니다.
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}