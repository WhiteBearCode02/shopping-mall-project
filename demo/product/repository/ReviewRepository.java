package com.example.shoppingmall.domain.product.repository;

import com.example.shoppingmall.domain.product.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 특정 상품의 리뷰 목록을 별점 높은 순, 최신 식별자 순으로 정렬하여 조회
    List<Review> findByProductIdOrderByRatingDescIdDesc(Long productId);
}