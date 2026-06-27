package com.example.shoppingmall.domain.order.service;

// [의존성 모듈 인터페이스 정의] 검증용 핵심 도메인 엔티티, 영속성 레포지토리 및 JUnit5 라이브러리 임포트
import com.example.shoppingmall.domain.order.entity.Order;
import com.example.shoppingmall.domain.order.repository.OrderRepository;
import com.example.shoppingmall.domain.product.entity.Product;
import com.example.shoppingmall.domain.product.repository.ProductRepository;
import com.example.shoppingmall.domain.user.entity.Role;
import com.example.shoppingmall.domain.user.entity.User;
import com.example.shoppingmall.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

// [단언문 정적 메서드 임포트] JUnit5의 성공/실패 검증 모듈 바인딩
import static org.junit.jupiter.api.Assertions.*;

/**
 * [주문 서비스 통합 테스트 레이어 (Order Service Integration Test Module)]
 * 실제 데이터베이스(인메모리 H2)와 스프링 컨테이너의 모든 빈(Bean) 의존 관계를 직접 로드하여,
 * 비관적 락 동시성 제어 및 주문 처리 핵심 비즈니스 논리의 정상 작동 여부를 자동 검증하는 테스트 클래스입니다.
 */
@SpringBootTest // [컨텍스트 로딩 알고리즘] 실제 구동 환경과 유사하도록 어플리케이션 컨텍스트를 통째로 로드하여 통합 테스트를 수행합니다.
@Transactional // [테스트 자원 격리 정책] 각 테스트 메서드 실행 완료 후 영속성 컨텍스트에 쌓인 변경 내역을 자동으로 완전히 롤백(Rollback)하여
               // DB를 초기화합니다.
class OrderServiceTest {

    // [의존성 자동 주입 파이프라인] 테스트 타겟이 되는 서비스 컴포넌트 및 검증용 데이터 세팅을 위한 레포지토리 주입
    @Autowired
    OrderService orderService;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ProductRepository productRepository;

    /**
     * [정상 주문 처리 및 데이터 정합성 검증 서브루틴]
     * 요구사항: 사용자가 유효한 범위 내에서 상품 주문을 정상 완료했을 때, 재고 수량이 주문량만큼 올바르게 차감되는지 추론 및 단언합니다.
     */
    @Test
    @DisplayName("상품 주문이 성공하면 재고가 주문 수량만큼 감소해야 한다")
    void 대용량_주문_및_재고_감소_테스트() {
        // given (테스트 환경 런타임 데이터 준비 단계)
        User user = User.builder()
                .email("test@example.com")
                .password("1234")
                .name("이현준")
                .role(Role.USER)
                .build();
        userRepository.save(user); // 검증용 회원 데이터 영속화

        Product product = Product.builder()
                .name("스프링 부트 완벽 가이드 북")
                .price(30000)
                .stockQuantity(10) // 최초 가용 재고 임계치를 10개로 설정
                .build();
        productRepository.save(product); // 검증용 상품 데이터 영속화

        int orderCount = 3; // 사용자의 요청 구매 수량 명세

        // when (실제 비즈니스 시나리오 가동 및 제어 런타임 단계)
        // OrderService 모듈의 비관적 쓰기 락 주문 트랜잭션을 가동하여 일련번호를 확보합니다.
        Long orderId = orderService.order(user.getId(), product.getId(), orderCount);

        // then (단언 알고리즘을 통한 정합성 최종 검증 단계)
        Order getOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("영속화된 주문 데이터를 탐색하지 못했습니다."));

        // 1. [상세 품목 건수 단언] 주문 마스터 엔티티 내 내포된 하위 상품 종류의 컬렉션 사이즈가 1건인지 검증합니다.
        assertEquals(1, getOrder.getOrderItems().size(), "주문한 상품 종류 수가 정확해야 합니다.");

        // 2. [총액 정합성 단언] 구매 단가와 수량의 곱연산 알고리즘이 영속화된 데이터베이스 실측값과 대응되는지 단언합니다.
        assertEquals(30000 * orderCount, getOrder.getOrderItems().get(0).getOrderPrice() * orderCount,
                "총 주문 가격이 일치해야 합니다.");

        // 3. [비즈니스 재고 차감 단언] 도메인 논리에 의해 영속성 컨텍스트 내 상품 재고가 정상 수치로 감축되었는지 확인합니다 (10 - 3 =
        // 7).
        assertEquals(7, product.getStockQuantity(), "주문 후 남은 재고 수량이 일치해야 합니다 (10 - 3 = 7).");
    }

    /**
     * [Fail-Fast 비즈니스 예외 예방 처리 검증 서브루틴]
     * 요구사항: 가용 재고 한계 수치를 상회하는 오버 트래픽 주문이 유입될 경우, 트랜잭션을 중단하고 안전하게 지정 예외를 도출하는지
     * 단언합니다.
     */
    @Test
    @DisplayName("상품 재고보다 많은 수량을 주문하면 예외가 발생해야 한다")
    void 재고_부족_주문_예외_테스트() {
        // given (테스트 환경 데이터 세팅 단계)
        User user = User.builder().email("user2@example.com").password("1234").name("홍길동").role(Role.USER).build();
        userRepository.save(user);

        Product product = Product.builder().name("덤벨 20kg").price(50000).stockQuantity(5).build(); // 가용 재고 5개 배치
        productRepository.save(product);

        int orderCount = 6; // 한계 임계치를 초과하는 6개 주문 시도

        // when & then (실행과 동시에 람다 서브루틴을 통한 예외 캡처 및 단언 검증)
        // 엔티티 내 removeStock 논리가 거부되어 IllegalArgumentException 예외 분기가 터져나오는지 추론합니다.
        assertThrows(IllegalArgumentException.class, () -> {
            orderService.order(user.getId(), product.getId(), orderCount);
        }, "재고가 부족하여 예외가 발생해야 합니다.");
    }
}