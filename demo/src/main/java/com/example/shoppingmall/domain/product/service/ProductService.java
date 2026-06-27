package com.example.shoppingmall.domain.product.service;

// [의존성 모듈 인터페이스 정의] 상품 비즈니스 트랜잭션 구동을 위한 핵심 엔티티 및 데이터 저장소 레이어 임포트
import com.example.shoppingmall.domain.product.entity.Product;
import com.example.shoppingmall.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * [상품 도메인 코어 비즈니스 로직 레이어 (Product Service Module)]
 * 상품의 등록, 단건 검색, 전체 리스트 스캔 파이프라인을 통제하며,
 * 데이터베이스 커넥션 풀 자원 분산 및 데이터 영속 상태를 통합 제어하는 서비스 컴포넌트입니다.
 */
@Service
@RequiredArgsConstructor
// [조회 트랜잭션 격리 최적화] 클래스 레벨에 readOnly = true 명세를 주입하여 하이버네이트 영속성 컨텍스트 플러시 동작을 생략,
// 메모리 및 탐색 알고리즘 성능을 극대화합니다.
@Transactional(readOnly = true)
public class ProductService {

    // [단일 책임 원칙에 기반한 저장소 컴포넌트 의존성 주입]
    private final ProductRepository productRepository;

    /**
     * [신규 상품 시스템 등록 및 영속화 알고리즘]
     * 요구사항 명세: 관리자 또는 백오피스 엔드포인트로부터 전달된 상품 도메인 모델을 데이터베이스 테이블에 반영합니다.
     * * @param product 웹 전송 계층의 DTO 분석을 거쳐 인스턴스화된 순수 상품 엔티티 객체
     * 
     * @return 영속성 컨텍스트 시퀀스 알고리즘에 의해 발급 완료된 상품 고유 식별 주키 (Product ID)
     */
    @Transactional // [쓰기 트랜잭션 전파 오버라이딩] 쓰기 격리 수준을 활성화하여 관계형 데이터베이스 테이블에 실제 INSERT 커밋 래치를 구동합니다.
    public Long saveProduct(Product product) {
        // 1. [영속성 가짜 저장 실행] 영속성 컨텍스트 내부 1차 캐시에 엔티티를 적재합니다.
        productRepository.save(product);

        // 2. [주키 식별 인자 반환] GenerationType.IDENTITY 메커니즘에 의해 영속화 즉시 발급된 주키 데이터를 호출 측에
        // 제공합니다.
        return product.getId();
    }

    /**
     * [고유 주키 기반 상품 단건 상세 검색 서브루틴]
     * 요구사항 명세: 사용자가 메인 진열대에서 피트니스 용품을 클릭했을 때 상세 명세 레이아웃을 바인딩하기 위해 단건 엔티티를 탐색합니다.
     * * @param productId 데이터베이스에서 탐색하고자 하는 타겟 상품 일련번호
     * 
     * @return 1차 캐시 또는 디스크 스캔을 통과하여 정합성이 확보된 완전한 Product 엔티티 객체
     */
    public Product findOne(Long productId) {
        // 1. [영속성 스캔 및 예외 추론] 리포지토리 인터페이스 유지를 통해 단건 데이터를 래핑 탐색합니다.
        // 2. [Fail-Fast 방어벽 가동] 만약 조작된 식별자나 존재하지 않는 ID 주입 시 하위 스레드로 오염 데이터가 전파되지 않도록
        // IllegalArgumentException 예외를 즉각 유발합니다.
        return productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다. ID: " + productId));
    }

    /**
     * [전체 상품 데이터 목록 인프라 스캔 서브루틴]
     * 요구사항 명세: 쇼핑몰 메인 게이트웨이 화면(index.html)이 로드될 때 진열장 내부를 채우기 위한 가용 상품 전건을 출력합니다.
     * * @return 데이터베이스 상품 테이블에 적재되어 상주 중인 전체 Product 엔티티 컬렉션 리스트
     */
    public List<Product> findProducts() {
        // 1. [전건 스캔 알고리즘 실행] 데이터베이스 엔티티 구조체를 풀 스캔하여 자바 메모리 배열 컬렉션으로 복원 및 반환합니다.
        return productRepository.findAll();
    }
}