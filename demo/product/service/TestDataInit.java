package com.example.shoppingmall.domain.product.service;

import com.example.shoppingmall.domain.product.entity.Product;
import com.example.shoppingmall.domain.product.repository.ProductRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TestDataInit {

    private final ProductRepository productRepository;

    /**
     * 의존성 주입이 완료된 후, 스프링 컨텍스트 런타임 시점에 자동으로 구동되는 메서드
     */
    @PostConstruct
    public void init() {
        // 1. 테스트용 상품 객체 생성 (빌더 패턴 적용)
        Product product1 = Product.builder()
                .name("고급 아일랜드 덤벨 세트")
                .price(120000)
                .stockQuantity(15)
                .build();

        Product product2 = Product.builder()
                .name("프리미엄 헬스 스트랩")
                .price(35000)
                .stockQuantity(50)
                .build();

        Product product3 = Product.builder()
                .name("단백질 보충제 2kg (초코맛)")
                .price(65000)
                .stockQuantity(8) // 동시성 제어 테스트를 위해 일부러 적은 수량 배치
                .build();

        // 2. 리포지토리를 통해 데이터베이스 영속화
        productRepository.save(product1);
        productRepository.save(product2);
        productRepository.save(product3);
    }
}