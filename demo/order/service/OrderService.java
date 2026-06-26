package com.example.shoppingmall.domain.order.service;

import com.example.shoppingmall.domain.order.entity.Order;
import com.example.shoppingmall.domain.order.entity.OrderItem;
import com.example.shoppingmall.domain.order.repository.OrderRepository;
import com.example.shoppingmall.domain.product.entity.Product;
import com.example.shoppingmall.domain.product.service.ProductService;
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
    private final ProductService productService;

    /**
     * 주문 생성 (핵심 비즈니스 알고리즘)
     */
    @Transactional // 여러 엔티티의 상태가 변경되므로 반드시 원자적 트랜잭션이 보장되어야 합니다.
    public Long order(Long userId, Long productId, int count) {

        // 1. 엔티티 조회 (회원 및 상품 정보 확보)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        Product product = productService.findOne(productId);

        // 2. [추론 과정] 상품 재고 감소 로직 호출
        // 엔티티 내부의 비즈니스 로직을 호출하여 상품 자체의 재고 수량을 깎습니다.
        product.removeStock(count);

        // 3. 주문 상품(OrderItem) 생성
        OrderItem orderItem = OrderItem.builder()
                .product(product)
                .orderPrice(product.getPrice())
                .count(count)
                .build();

        // 4. 주문(Order) 객체 생성 및 연관관계 조립
        Order order = new Order();
        order.setUser(user);
        order.addOrderItem(orderItem); // 연관관계 편의 메서드로 안전하게 연결

        // 5. 주문 저장
        // OrderItem은 Order에 cascade = CascadeType.ALL 설정이 되어 있으므로 함께 저장됩니다.
        orderRepository.save(order);

        return order.getId();
    }
}