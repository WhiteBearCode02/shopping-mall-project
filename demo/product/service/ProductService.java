package com.example.shoppingmall.domain.product.service;

import com.example.shoppingmall.domain.product.entity.Product;
import com.example.shoppingmall.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 전체 조회 성능 최적화
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * 상품 등록 (관리자 전용 기능으로 확장될 예정)
     */
    @Transactional // 데이터 추가가 일어나므로 트랜잭션을 활성화합니다.
    public Long saveProduct(Product product) {
        productRepository.save(product);
        return product.getId();
    }

    /**
     * 상품 단건 조회 (상세 페이지)
     */
    public Product findOne(Long productId) {
        // [추론 검증] 존재하지 않는 상품 ID 조회 시, 예외를 명확히 던져 시스템 안정성을 확보합니다.
        return productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다. ID: " + productId));
    }

    /**
     * 상품 전체 목록 조회
     */
    public List<Product> findProducts() {
        return productRepository.findAll();
    }
}