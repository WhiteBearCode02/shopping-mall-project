package com.example.shoppingmall.domain.product.repository;

import com.example.shoppingmall.domain.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    // 기본 CRUD 기능 외에 특정 상품명으로 검색하는 기능 등이 필요할 때 여기에 확장합니다.
}