package com.example.shoppingmall.domain.order.repository;

// [의존성 모듈 인터페이스 정의] 영속성 컨텍스트 관리를 위한 엔티티 클래스 및 JPA 인프라 라이브러리 임포트
import com.example.shoppingmall.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * [주문 도메인 데이터 영속성 레이어 (Order Repository Module)]
 * 관계형 데이터베이스(RDB)의 주문(Orders) 테이블과 상호작용하며,
 * CRUD(Create, Read, Update, Delete) 관련 핵심 쿼리 실행 및 영속성 컨텍스트 제어를 전담하는 저장소
 * 인터페이스입니다.
 */
@Repository
// [데이터 액세스 레이어 영속화] 스프링 컴포넌트 스캔 매커니즘이 이 인터페이스를 감지하여 런타임에 동적 프록시 빈(Bean)으로
// 인스턴스화하도록 마킹합니다.
public interface OrderRepository extends JpaRepository<Order, Long> {

    // [확장 파이프라인 영역]
    // 기본적으로 JpaRepository를 상속받음으로써 save(), findById(), deleteById() 등의 코어 서브루틴은 코드가
    // 없어도 자동 생성됩니다.
    // 향후 특정 회원의 주문 이력 조회(예: findByUserId) 등의 도메인 주도 쿼리 메서드가 필요할 경우 이 레이어 내부에 명세를
    // 확장합니다.
}