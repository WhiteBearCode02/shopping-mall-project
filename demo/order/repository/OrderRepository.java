package com.example.shoppingmall.domain.order.repository;

import com.example.shoppingmall.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    // 사용자의 주문 내역 전체 조회 등은 나중에 여기에 쿼리 메서드로 확장할 수 있습니다.
}