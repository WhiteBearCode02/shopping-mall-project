package com.example.shoppingmall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
// [추론 및 컨텍스트 격리 해결] 스프링이 프로젝트 내의 모든 JPA 리포지토리를 누락 없이 강제 스캔하도록 범위를 확장합니다.
@EnableJpaRepositories(basePackages = "com.example.shoppingmall")
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}
}