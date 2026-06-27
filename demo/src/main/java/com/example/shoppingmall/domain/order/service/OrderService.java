package com.example.shoppingmall.domain.order.service;

// [의존성 모듈 인터페이스 정의] 비즈니스 트랜잭션 수립을 위한 핵심 도메인 엔티티 및 저장소(Repository) 임포트
import com.example.shoppingmall.domain.order.entity.Order;
import com.example.shoppingmall.domain.order.entity.OrderItem;
import com.example.shoppingmall.domain.order.repository.OrderRepository;
import com.example.shoppingmall.domain.product.entity.Product;
import com.example.shoppingmall.domain.product.repository.ProductRepository;
import com.example.shoppingmall.domain.user.entity.User;
import com.example.shoppingmall.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * [주문 도메인 핵심 비즈니스 로직 레이어 (Order Service Module)]
 * 회원, 상품 계층을 아우르는 복합 도메인 오케스트레이션을 담당하며,
 * 비관적 쓰기 락(Pessimistic Write Lock) 알고리즘을 제어하여 데이터 정합성을 보장하는 핵심 런타임 서비스입니다.
 */
@Service
@RequiredArgsConstructor
// [트랜잭션 기본 방어벽 가동] 클래스 레벨에 읽기 전용 트랜잭션을 적용하여 커넥션 풀 자원 소모를 방지하고 최적화를 수행합니다.
@Transactional(readOnly = true)
public class OrderService {

        // [단일 책임 원칙에 기반한 저장소 캡슐화 의존성 주입]
        private final OrderRepository orderRepository;
        private final UserRepository userRepository;
        private final ProductRepository productRepository; // [동시성 락 바인딩 용도] 상품의 로우 레벨 락 획득을 위해 직접 참조를 승인합니다.

        /**
         * [동시성 제어 기반 주문 생성 핵심 트랜잭션 수립 알고리즘]
         * 다중 스레드 환경에서 한정된 재고 자원을 안전하게 차감하고 최종 주문 명세를 데이터베이스에 영속화하는 서브루틴입니다.
         * * @param userId 주문을 요청한 고객 고유 일련번호
         * 
         * @param productId 구매 타겟 피트니스 상품 고유 일련번호
         * @param count     사용자가 선택한 구매 요청 개수
         * @return 영속성 컨텍스트에 의해 ID 생성이 완료된 최종 주문 일련번호(Order ID)
         */
        @Transactional // [쓰기 트랜잭션 분리] 기본 readOnly 정책을 오버라이딩하여 데이터 변경 및 DB 커밋 커넥션을 확보합니다.
        public Long order(Long userId, Long productId, int count) {

                // 1. [회원 도메인 검증 및 조회] 영속성 컨텍스트에서 유저 정보를 탐색하고, 부재 시 Fail-Fast 매커니즘에 따라 즉시 런타임
                // 예외를 터트립니다.
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

                // 2. [인프라 락 획득 - 핵심 기술 구간] 일반 조회가 아닌 비관적 쓰기 락(SELECT ... FOR UPDATE) 조건이 내장된 조회
                // 서브루틴을 구동합니다.
                // 이 타이밍에 데이터베이스 내 해당 상품 로우(Row)의 독점 잠금을 획득하므로, 동시에 접근하는 다른 사용자 트랜잭션은 락 해제 시까지
                // 대기 열에 대치됩니다.
                Product product = productRepository.findByIdWithPessimisticLock(productId)
                                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

                // 3. [도메인 엔티티 캡슐화 로직 구동] 데이터 정합성이 보장된 상태에서 상품 객체 내부의 비즈니스 논리를 가동해 요청 개수만큼 재고를
                // 차감합니다.
                // 만약 차감 도중 상품 내 재고 임계치를 하회하면 엔티티 내부 알고리즘이 IllegalArgumentException을 던져 트랜잭션이 전체
                // 롤백됩니다.
                product.removeStock(count);

                // 4. [주문 상세 컴포넌트 조립] 빌더 패턴 아키텍처를 가동하여 어떤 상품을 얼마의 단가로 몇 개 구매했는지 상세 명세 내역을
                // 인스턴스화합니다.
                OrderItem orderItem = OrderItem.builder()
                                .product(product)
                                .orderPrice(product.getPrice())
                                .count(count)
                                .build();

                // 5. [연관관계 편의 메서드 체이닝] 최종 주문 객체를 인스턴스화하고 회원 객체 및 앞서 조립한 주문 상세 내역 간의 다차원 연관관계를
                // 결합합니다.
                Order order = Order.builder()
                                .user(user)
                                .build();
                order.addOrderItem(orderItem);

                // 6. [데이터 영속성 레이어 세이브 명령 실행] 주문 엔티티의 영속화를 요청합니다. CascadeType.ALL 명세에 의거해 내부
                // orderItem도 연쇄 저장됩니다.
                // 해당 메서드가 종료되는 순간 스프링 AOP 프록시 메커니즘이 DB 커밋을 유발하며, 이때 획득했던 상품 로우의 비관적 락이 자동으로 완전
                // 해제됩니다.
                orderRepository.save(order);

                return order.getId();
        }
}