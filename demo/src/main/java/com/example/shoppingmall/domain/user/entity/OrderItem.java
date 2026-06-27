package com.example.shoppingmall.domain.user.entity;

import com.example.shoppingmall.domain.product.entity.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "order_items")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product; // 주문 상품

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order; // 주문 정보

    private Integer orderPrice; // 주문 당시 가격
    private Integer count; // 주문 수량

    @Builder
    public OrderItem(Product product, Integer orderPrice, Integer count) {
        this.product = product;
        this.orderPrice = orderPrice;
        this.count = count;
    }

    public void assignOrder(Order order) {
        this.order = order;
    }
}