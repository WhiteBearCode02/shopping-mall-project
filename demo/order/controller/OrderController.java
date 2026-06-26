package com.example.shoppingmall.domain.order.controller;

import com.example.shoppingmall.domain.order.service.OrderService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    /**
     * 주문 요청 API
     */
    @PostMapping
    public String createOrder(@RequestBody OrderRequestDto request) {
        try {
            Long orderId = orderService.order(request.getUserId(), request.getProductId(), request.getCount());
            return "주문이 성공적으로 완료되었습니다. 주문 번호: " + orderId;
        } catch (IllegalArgumentException e) {
            // 재고가 부족하거나 정보가 올바르지 않을 때의 에러 핸들링
            return "주문 실패: " + e.getMessage();
        }
    }

    /**
     * 데이터를 안전하게 받기 위한 단순 DTO 객체 구조
     */
    @Getter
    public static class OrderRequestDto {
        private Long userId;
        private Long productId;
        private int count;
    }
}