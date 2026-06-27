package com.example.shoppingmall.domain.product.service;

// [의존성 모듈 인터페이스 정의] 초기 데이터 적재를 위한 상품 도메인 엔티티 및 저장소 레이어 라이브러리 임포트
import com.example.shoppingmall.domain.product.entity.Product;
import com.example.shoppingmall.domain.product.repository.ProductRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * [런타임 테스트 데이터 초기화 컴포넌트 (Test Data Initialization Module)]
 * 웹 애플리케이션 프레임워크 부트스트랩 시점에 가용 상품 초기 레코드를 데이터베이스 테이블에
 * 자동으로 사전 적재하여 프론트엔드 연동 및 동시성 락 테스트 환경을 구축하는 인프라 클래스입니다.
 */
@Component // [컴포넌트 스캔 바인딩] 스프링 IoC 컨테이너가 싱글톤 빈(Bean)으로 등록 및 수집하도록 마킹합니다.
@RequiredArgsConstructor // [생성자 자동 주입] final 필드로 지정된 영속성 저장소 객체의 의존성 주입 파이프라인을 형성합니다.
public class TestDataInit {

        // [단일 책임 원칙에 기반한 데이터 액세스 레이어 의존성 주입]
        private final ProductRepository productRepository;

        /**
         * [스프링 빈 라이프사이클 이벤트 런타임 구동 서브루틴]
         * 작동 원리: 스프링 컨테이너에 의해 빈 인스턴스가 생성되고 final 의존성 주입(DI)이 완전히 완료된 직후,
         * 컨텍스트 개시 단계에서 이 서브루틴이 프레임워크 런타임에 의해 단 1회 강제 호출됩니다.
         */
        @PostConstruct
        public void init() {
                // 1. [테스트용 상품 객체 뼈대 생성 - 빌더 패턴 캡슐화 알고리즘]
                // 각 객체의 필드 안전성을 확보하며 피트니스 쇼핑몰의 주력 상품 라인업을 인스턴스화합니다.
                Product product1 = Product.builder()
                                .name("고급 아일랜드 덤벨 세트")
                                .price(120000)
                                .stockQuantity(15)
                                .build();

                Product product2 = Product.builder()
                                .name("프리미엄 헬스 스트랩")
                                .price(35000)
                                .stockQuantity(50)
                                .build();

                Product product3 = Product.builder()
                                .name("단백질 보충제 2kg (초코맛)")
                                .price(65000)
                                .stockQuantity(8) // 동시성 락 및 갱신 분실 데이터 레이스 테스트를 유도하기 위해 가용 수량을 8개 임계치로 의도적 제한
                                .build();

                // 2. [JPA 영속성 파이프라인 가동]
                // 리포지토리 인터페이스 프록시의 save 서브루틴을 호출하여 하이버네이트 1차 캐시에 적재한 뒤
                // 데이터베이스 세션 플러시를 거쳐 데이터 테이블에 최종 레코드로 영속화합니다.
                productRepository.save(product1);
                productRepository.save(product2);
                productRepository.save(product3);
        }
}