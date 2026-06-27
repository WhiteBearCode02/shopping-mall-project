public class Review {
    
}
package com.example.shoppingmall.domain.product.entity;

import com.example.shoppingmall.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "reviews", indexes = {
        // [추론 및 성능 설계] 특정 상품의 리뷰를 최신순 또는 평점순으로 빠르게 조회하기 위한 복합 인덱스 설정
        @Index(name = "idx_product_rating_id", columnList = "product_id, rating, review_id DESC")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer rating; // 별점 (1 ~ 5점)

    @Column(nullable = false, length = 1000)
    private String content; // 리뷰 내용

    @Builder
    public Review(Product product, User user, Integer rating, String content) {
        this.product = product;
        this.user = user;
        this.rating = rating;
        this.content = content;
    }
}