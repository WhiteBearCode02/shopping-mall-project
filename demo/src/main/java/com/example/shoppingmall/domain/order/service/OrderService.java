package com.example.shoppingmall.domain.order.service;

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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository; // 잠금 조회를 위해 레포지토리 직접 참조 수용

    /**
     * 동시성이 제어되는 주문 생성 로직
     */
    @Transactional
    public Long order(Long userId, Long productId, int count) {

        // 1. 회원 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 2. [추론 및 수정] 일반 조회가 아닌 비관적 락이 설정된 조회 알고리즘을 사용합니다.
        // 이 타이밍에 해당 상품 Row에 Lock이 걸려 다른 트랜잭션은 대기하게 됩니다.
        Product product = productRepository.findByIdWithPessimisticLock(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

        // 3. 재고 감소 (엔티티 내 비즈니스 로직)
        product.removeStock(count);

        // 4. 주문 상품 생성
        OrderItem orderItem = OrderItem.builder()
                .product(product)
                .orderPrice(product.getPrice())
                .count(count)
                .build();

        // 5. 주문 생성 및 연관관계 조립
        Order order = new Order();
        order.setUser(user);
        order.addOrderItem(orderItem);

        // 6. 주문 저장 (트랜잭션이 종료되면서 커밋되고 락이 해제됩니다)
        orderRepository.save(order);

        return order.getId();
    }
}