package com.example.shoppingmall.domain.product.entity;

// [의존성 모듈 인터페이스 정의] JPA 영속성 어노테이션 및 롬복 컴파일러 링킹
import jakarta.persistence.*;
import lombok.*;

/**
 * [상품 도메인 코어 엔티티 레이어 (Product Entity Module)]
 * 쇼핑몰의 진열대에 올라가는 헬스기구, 보충제 등의 핵심 상품 데이터를 데이터베이스 테이블과 매핑하는 ORM 객체입니다.
 */
@Entity // [JPA 맵핑] 하이버네이트 엔진이 기동될 때 이 클래스를 기반으로 'product' 물리 테이블을 스캔 및 생성합니다.
@Getter // 필드 조회를 위한 Getter 서브루틴 자동 생성
@NoArgsConstructor(access = AccessLevel.PROTECTED) // [보안 캡슐화] JPA 프록시 객체 생성을 위한 기본 생성자 개방(무분별한 인스턴스화 방어를 위해 PROTECTED
                                                   // 격리)
@AllArgsConstructor // Builder 패턴 가동을 위한 전체 인자 생성자
@Builder // DTO로부터 변환될 때 안전하고 직관적인 객체 조립을 지원하는 빌더 패턴 적용
public class Product {

    @Id // [Primary Key 바인딩] 데이터베이스 테이블의 주키(PK) 식별자
    @GeneratedValue(strategy = GenerationType.IDENTITY) // [시퀀스 전략] MySQL의 AUTO_INCREMENT처럼 DB 엔진에 식별자 발급을 위임합니다.
    @Column(name = "product_id")
    private Long id;

    // 상품의 이름 (예: 최고급 아일랜드 덤벨)
    @Column(nullable = false)
    private String name;

    // 상품 단가
    @Column(nullable = false)
    private Integer price;

    // 현재 물리적으로 남아있는 재고 수량
    @Column(nullable = false)
    private Integer stockQuantity;

    /**
     * [비즈니스 로직 서브루틴] 핵심 도메인 재고 차감 알고리즘
     * 아키텍처 원칙: 서비스(Service) 계층에서 재고를 직접 빼지 않고, 객체 지향 설계(DDD) 원칙에 따라 엔티티 스스로 상태를
     * 변경하도록 캡슐화합니다.
     * 
     * @param quantity 고객이 주문하고자 요청한 상품의 수량
     */
    public void removeStock(int quantity) {
        // 1. 기존 재고에서 주문 수량을 차감하여 가용 잔여 재고를 추론합니다.
        int restStock = this.stockQuantity - quantity;

        // 2. [비즈니스 방어벽] 만약 잔여 재고가 0 미만으로 떨어지면, 즉시 트랜잭션을 롤백시키는 예외를 던집니다.
        if (restStock < 0) {
            throw new IllegalArgumentException("상품의 재고가 부족합니다. (현재 재고: " + this.stockQuantity + ")");
        }

        // 3. 검증을 통과했다면 안전하게 재고 상태를 갱신합니다.
        this.stockQuantity = restStock;
    }
}