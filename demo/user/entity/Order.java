package com.example.shoppingmall.domain.order.entity;

import com.example.shoppingmall.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // 주문자

    // 양방향 연관관계 설정 시 CascadeType.ALL을 통해 주문 저장 시 주문 상품도 함께 영속화합니다.
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    public void setUser(User user) {
        this.user = user;
    }

    /**
     * 연관관계 편의 메서드
     */
    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.assignOrder(this);
    }
}