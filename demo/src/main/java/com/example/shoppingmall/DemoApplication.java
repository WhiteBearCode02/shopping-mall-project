package com.example.shoppingmall;

// [의존성 모듈 인터페이스 정의] 스프링 부트 애플리케이션 구동 및 컴포넌트 스캔 자동 설정을 위한 코어 라이브러리 임포트
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * [애플리케이션 최상위 메인 부트스트랩 레이어 (Main Application Bootstrap Module)]
 * 쇼핑몰 웹 서버 시스템의 전체적인 라이프사이클을 개시하고, 내장 톰캣(Embedded Tomcat) 서블릿 컨테이너를 구동하며,
 * 전역 패키지에 파편화된 빈(Bean)과 영속성 아티팩트들을 수집하는 마스터 진입점 클래스입니다.
 */
@SpringBootApplication(scanBasePackages = "com.example.shoppingmall")
// [컴포넌트 스캔 체인 최적화] @Component, @RestController, @Service 등 웹 비즈니스 모듈의 탐색 링킹 시작점
// 주소를 루트 패키지로 지정합니다.

@EnableJpaRepositories(basePackages = "com.example.shoppingmall")
// [JPA 레포지토리 스캔 동기화] 인프라 영속성 계층 인터페이스(OrderRepository, ProductRepository 등)의 동적
// 프록시 생성 범위를 강제 제어합니다.

@EntityScan(basePackages = "com.example.shoppingmall")
// [ORM 도메인 데이터 모델 스캔 동기화] 하이버네이트 소스 컴파일러가 데이터베이스 테이블과 매핑할 @Entity 클래스(Order,
// Product, Review 등)들을 탐색하도록 명세를 바인딩합니다.

public class DemoApplication {

    /**
     * [자바 가상 머신(JVM) 메인 실행 엔트리포인트 서브루틴]
     * 어플리케이션 컨텍스트를 로딩하고 IoC(제어의 역전) 컨테이너 부트스트래핑 알고리즘을 최종 수행합니다.
     * * @param args 실행 시점에 외부로부터 유입될 수 있는 런타임 환경 파라미터 배열 인자
     */
    public static void main(String[] args) {
        // [스프링 인프라 구동 가동] 메인 클래스 메타데이터와 인자를 넘겨 백엔드 엔진 프레임워크를 런타임 메모리에 안착시킵니다.
        SpringApplication.run(DemoApplication.class, args);
    }
}