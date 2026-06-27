// [구조 정형화 알고리즘] 윈도우 파일 시스템 디렉터리와 완전히 부합하도록 주문 도메인 엔티티 패키지 경로를 정정합니다.
package com.example.shoppingmall.domain.order.entity;

// [의존성 모듈 인터페이스 정의] 다차원 연관관계 매핑 및 캡슐화 인프라 구동을 위한 자카르타 퍼시스턴스 및 롬복 임포트
import com.example.shoppingmall.domain.product.entity.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * [주문 상세 상품 도메인 엔티티 계층 (Order Item Entity Module)]
 * 데이터베이스의 order_items 테이블과 매핑되며, 마스터 주문(Order)에 종속되어
 * 어떤 피트니스 상품(Product)을 얼마의 스냅샷 가격으로 몇 개 구매했는지 세부 이력을 관리하는 데이터 모델입니다.
 */
@Entity
@Getter
@Table(name = "order_items")
// [무분별한 객체 생성 방어 알고리즘] 비즈니스 무결성을 사수하기 위해 외부에서 인자 없는 new 객체 생성을 PROTECTED로 격리
// 차단합니다.
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    // [기본키 식별 자원 정의] 관계형 데이터베이스의 시퀀스 자동 증가(IDENTITY) 메커니즘을 영속성 주키 알고리즘으로 동기화합니다.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long id;

    // [다대일 상품 연관관계 매핑 모듈]
    // FetchType.LAZY(지연 로딩) 전략을 도입하여 품목 조회 시 연관된 대용량 상품(Product) 정보가 불필요하게 무조건
    // 조인(Join)되는 인프라 과부하를 방지합니다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id") // 데이터베이스 테이블 내 상품 외래키(FK) 컬럼을 바인딩합니다.
    private Product product;

    // [다대일 부모 주문 양방향 연관관계 매핑 모듈]
    // 지연 로딩을 강제하여 객체 그래프 탐색 시에만 데이터를 호출하도록 격리하며, 외래키 주인의 책임을 수행합니다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id") // 데이터베이스 테이블 내 주문 마스터 외래키(FK) 컬럼을 바인딩합니다.
    private Order order;

    // [비즈니스 도메인 명세 필드] 주문 거래 체결 당시의 가격 (향후 상품 원가 변동 여파와 무관한 스냅샷 데이터 보존 목적)
    private Integer orderPrice;

    // [비즈니스 도메인 명세 필드] 해당 상품을 구매한 수량 데이터 필드
    private Integer count;

    /**
     * [엔티티 안전 생성을 위한 빌더 패턴 아키텍처 스펙]
     * 점진적 생성자 매개변수 바인딩 오차를 방지하고 생성 시점에 정합성이 확보된 완성형 객체만 인스턴스화하도록 강제합니다.
     */
    @Builder
    public OrderItem(Product product, Integer orderPrice, Integer count) {
        this.product = product;
        this.orderPrice = orderPrice;
        this.count = count;
    }

    /**
     * [부모 주문 식별자 결합 연관관계 편의 서브루틴]
     * 마스터 객체인 Order 엔티티 내부 addOrderItem 편의 메서드와 맞물려 가동되는 연동 메서드입니다.
     * 외래키 필드인 order에 부모 인스턴스를 주입하여 데이터베이스 영속화 시 정확한 관계를 바인딩하도록 조율합니다.
     * * @param order 이 상세 품목을 소유하고 지배하는 상위 마스터 주문 엔티티 인스턴스
     */
    public void assignOrder(Order order) {
        // 객체 지향 메모리 정합성 갱신을 위해 상위 주문 마스터 객체 참조 주소를 캡슐화 필드에 바인딩합니다.
        this.order = order;
    }
}