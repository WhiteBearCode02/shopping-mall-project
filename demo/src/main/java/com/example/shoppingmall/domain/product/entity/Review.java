// [구조 정형화 알고리즘] 자바 표준 명세 명시에 의거하여 패키지 주소 선언문을 파일 최상단으로 강제 전치합니다.
package com.example.shoppingmall.domain.product.entity;

// [의존성 모듈 인터페이스 정의] 객체-관계 매핑(ORM) 명세 준수 및 빌더 패턴 캡슐화를 위한 자카르타 퍼시스턴스 및 롬복 임포트
import com.example.shoppingmall.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * [상품 리뷰 도메인 엔티티 계층 (Product Review Entity Module)]
 * 쇼핑몰 내부 데이터베이스의 리뷰(Reviews) 테이블과 1:1 매핑되어
 * 회원이 구매한 상품에 대한 평점 및 텍스트 데이터를 관리하고 영속 상태를 통제하는 코어 데이터 모델 클래스입니다.
 */
@Entity
@Getter
// [데이터베이스 테이블 명세 파이프라인] 복합 인덱싱을 통해 인프라 처리 성능을 극대화합니다.
@Table(name = "reviews", indexes = {
        // [복합 인덱스(Compound Index) 최적화 설계]
        // 특정 피트니스 기구(product_id)에 누적된 리뷰 목록을 평점순(rating) 또는 최신순(review_id DESC)으로
        // 브라우저 화면에 빠르게 스캔하여 렌더링하기 위해 디스크 정렬 구조를 명시적으로 인덱싱 알고리즘에 바인딩합니다.
        @Index(name = "idx_product_rating_id", columnList = "product_id, rating, review_id DESC")
})
// [무분별한 객체 생성 방어 알고리즘] 캡슐화 원칙에 의거하여 생성자 접근 제어자를 PROTECTED로 래핑, 빈 객체 생성 오류를
// 차단합니다.
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review {

    // [기본키 식별 자원 정의] 관계형 데이터베이스의 시퀀스 자동 증가(IDENTITY) 메커니즘을 영속성 컨텍스트 주키(Primary Key)
    // 알고리즘으로 동기화합니다.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    // [다대일 단방향 객체 연관관계 캡슐화 모듈]
    // FetchType.LAZY(지연 로딩) 전략을 도입하여 리뷰 조회 시 연관된 상품 객체를 프록시(Proxy) 상태로 격리, N+1 부하
    // 문제를 원천 차단합니다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // [다대일 단방향 회원 연관관계 캡슐화 모듈]
    // 지연 로딩 알고리즘을 강제하여 트래픽 집중 시 회원 테이블과의 불필요한 아우터 조인(Outer Join) 현상을 통제합니다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // [비즈니스 도메인 명세 필드] 평점 데이터 제어 (1점 ~ 5점 임계치 관리 타겟)
    @Column(nullable = false)
    private Integer rating;

    // [비즈니스 도메인 명세 필드] 리뷰 텍스트 데이터 제어 (VARCHAR(1000) 바운더리 매핑)
    @Column(nullable = false, length = 1000)
    private String content;

    /**
     * [엔티티 안전 생성을 위한 빌더 패턴 아키텍처 스펙]
     * 점진적 생성자 매개변수 누락 오류를 방지하고 생성 시점에 정합성이 확보된 완전한 객체 모델만 런타임에 인스턴스화하도록 강제합니다.
     */
    @Builder
    public Review(Product product, User user, Integer rating, String content) {
        this.product = product;
        this.user = user;
        this.rating = rating;
        this.content = content;
    }
}