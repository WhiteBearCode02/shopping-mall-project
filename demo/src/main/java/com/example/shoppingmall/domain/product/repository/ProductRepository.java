package com.example.shoppingmall.domain.product.repository;

// [의존성 모듈 인터페이스 정의] 영속성 제어 및 동시성 잠금 메커니즘을 위한 핵심 JPA 라이브러리 및 엔티티 임포트
import com.example.shoppingmall.domain.product.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * [상품 도메인 데이터 영속성 레이어 (Product Repository Module)]
 * 관계형 데이터베이스(RDB)의 상품(Products) 테이블과 직접 매핑되어 원시 쿼리 및
 * 객체 영속성 컨텍스트(Persistence Context) 스캔 서브루틴을 제어하는 인프라 저장소 인터페이스입니다.
 */
@Repository
// [데이터 액세스 레이어 영속화] 스프링 컴포넌트 스캔 매커니즘이 이 인터페이스를 동적 프록시 빈으로 인스턴스화하도록 바인딩합니다.
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * [동시성 자원 제어 전용 - 비관적 쓰기 락 조회 알고리즘]
     * 요구사항 명세: 다중 스레드가 한정된 상품 재고 로우(Row)에 동시 접근하여 수량을 차감할 때 발생하는 동시성 충돌을 차단합니다.
     * 작동 원리: 트랜잭션이 시작되고 이 메서드가 호출되면, 데이터베이스 엔진은 로우 레벨 잠금(Lock)을 걸기 위해 'SELECT ...
     * FOR UPDATE' 구문을 즉시 가동합니다.
     * * @param id 동시성 제어 잠금을 획득하고자 하는 타겟 피트니스 상품의 고유 일련번호 (Primary Key)
     * 
     * @return 런타임 NullPointerException 예외 전파를 우아하게 차단하고 감싸 안는 Optional 구조의 상품 엔티티
     *         객체
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE) // [하이버네이트 락 프로토콜] DB 벤더별 배타적 잠금(Exclusive Lock) 명세를 런타임에 강제합니다.
    @Query("select p from Product p where p.id = :id") // [JPQL 객체지향 쿼리 명세] 엔티티 속성을 추상화하여 주키 기반 조회를 가동합니다.
    Optional<Product> findByIdWithPessimisticLock(@Param("id") Long id);
}