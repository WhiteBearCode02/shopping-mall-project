package com.example.shoppingmall.domain.order.service;

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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional // 테스트 완료 후 데이터베이스를 자동으로 롤백하여 격리성을 유지합니다.
class OrderServiceTest {

    @Autowired
    OrderService orderService;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ProductRepository productRepository;

    @Test
    @DisplayName("상품 주문이 성공하면 재고가 주문 수량만큼 감소해야 한다")
    void 대용량_주문_및_재고_감소_테스트() {
        // given (준비 단계)
        User user = User.builder()
                .email("test@example.com")
                .password("1234")
                .name("이현준")
                .role(Role.USER)
                .build();
        userRepository.save(user);

        Product product = Product.builder()
                .name("스프링 부트 완벽 가이드 북")
                .price(30000)
                .stockQuantity(10)
                .build();
        productRepository.save(product);

        int orderCount = 3;

        // when (실행 단계)
        Long orderId = orderService.order(user.getId(), product.getId(), orderCount);

        // then (검증 단계)
        Order getOrder = orderRepository.findById(orderId).orElseThrow();

        assertEquals(1, getOrder.getOrderItems().size(), "주문한 상품 종류 수가 정확해야 합니다.");
        assertEquals(30000 * orderCount, getOrder.getOrderItems().get(0).getOrderPrice() * orderCount,
                "총 주문 가격이 일치해야 합니다.");
        assertEquals(7, product.getStockQuantity(), "주문 후 남은 재고 수량이 일치해야 합니다 (10 - 3 = 7).");
    }

    @Test
    @DisplayName("상품 재고보다 많은 수량을 주문하면 예외가 발생해야 한다")
    void 재고_부족_주문_예외_테스트() {
        // given
        User user = User.builder().email("user2@example.com").password("1234").name("홍길동").role(Role.USER).build();
        userRepository.save(user);

        Product product = Product.builder().name("덤벨 20kg").price(50000).stockQuantity(5).build();
        productRepository.save(product);

        int orderCount = 6; // 재고는 5개인데 6개 주문 시도

        // when & then (검증)
        assertThrows(IllegalArgumentException.class, () -> {
            orderService.order(user.getId(), product.getId(), orderCount);
        }, "재고가 부족하여 예외가 발생해야 합니다.");
    }
}