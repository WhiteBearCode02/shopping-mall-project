// [구조 정형화 알고리즘] 실제 물리 폴더 아키텍처와 완벽하게 부합하도록 주문 도메인 패키지 주소 명세를 정정합니다.
package com.example.shoppingmall.domain.order.entity;

// [의존성 모듈 인터페이스 정의] 객체-관계 매핑(ORM) 명세 준수 및 빌더 캡슐화를 위한 자카르타 퍼시스턴스 및 롬복 임포트
import com.example.shoppingmall.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

/**
 * [주문 도메인 핵심 엔티티 계층 (Order Core Entity Module)]
 * 데이터베이스의 orders 테이블과 매핑되어 고객의 결제 명세 메인 마스터 정보를 관리하고,
 * 하위 주문 상세 내역(OrderItem) 컬렉션 간의 복합 연관관계를 중재하는 영속성 코어 데이터 모델입니다.
 */
@Entity
@Getter
@Table(name = "orders") // [RDB 스키마 바인딩] 예약어 충돌 예방을 위해 테이블 명세를 명시적으로 동기화
// [무분별한 외부 객체 생성 제어] 영속성 프록시 계층 레이어의 정상 구동(가짜 객체 생성)을 보장하되, 외부에서의 무분별한 new 키워드
// 생성을 PROTECTED로 격리 차단합니다.
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    // [기본키 식별 자원 정의] 데이터베이스 내부 인메모리 시퀀스 자동 증가(IDENTITY) 메커니즘을 주키 알고리즘으로 매핑합니다.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    // [다대일 회원 연관관계 매핑 모듈]
    // FetchType.LAZY(지연 로딩) 전략을 강제하여 무분별한 유저 데이터 사전 조회(EAGER)로 인한 메모리 병목 및 N+1 부하를
    // 원천 차단합니다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // 데이터베이스 테이블 내 외래키(FK) 컬럼 주소를 결합합니다.
    private User user;

    // [일대다 주문 상세 내역 양방향 연관관계 모듈]
    // mappedBy = "order": 연관관계의 주인이 하위 엔티티(OrderItem)임을 명시하여 외래키 중복 제어 권한을 양도합니다.
    // cascade = CascadeType.ALL: 영속성 전이 알고리즘을 활성화하여 마스터인 Order가 save()될 때 내포된
    // orderItems 배열 컬렉션도 연쇄적으로 함께 영속화되도록 인프라를 바인딩합니다.
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    /**
     * [엔티티 안전 생성을 위한 불변 객체 빌더 패턴 아키텍처 스펙]
     * 생성 시점에 필수 인자(User 등) 누락을 원천 차단하기 위해 롬복 어노테이션 대신 명시적 생성자에 빌더 모듈을 수립합니다.
     */
    @Builder
    public Order(User user) {
        this.user = user;
    }

    /**
     * [회원 연관관계 바인딩 캡슐화 세터]
     * 외부 레이어에서 식별 관계를 명확히 조립하기 위한 수정자 서브루틴입니다.
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * [다차원 객체 연관관계 편의 메서드 데이터 무결성 알고리즘]
     * 비즈니스 로직 도중 객체 그래프 탐색 시 발생할 수 있는 메모리상 데이터 불일치(한쪽만 데이터가 채워지는 현상)를 원천 차단합니다.
     * 하나의 메서드 호출만으로 자바 메모리 컬렉션(orderItems.add)과 하위 엔티티의 외래키 참조
     * 대상(orderItem.assignOrder)을 동시에 완벽 동기화합니다.
     * * @param orderItem 이 주문과 결합하고자 하는 개별 피트니스 용품 구매 상세 내역 인스턴스
     */
    public void addOrderItem(OrderItem orderItem) {
        // 1. 객체 지향 메모리 정합성을 위해 부모 컬렉션 리스트 내부에 하위 주문 상품 객체를 수집합니다.
        orderItems.add(orderItem);

        // 2. 하위 주문 상품 객체 내부 외래키 참조 필드에 자기 자신(this)을 주입하여 양방향 데이터 정합성 파이프라인을 갱신합니다.
        orderItem.assignOrder(this);
    }
}