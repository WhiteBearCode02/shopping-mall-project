package com.example.shoppingmall.domain.product.service;

import com.example.shoppingmall.domain.product.entity.Product;
import com.example.shoppingmall.domain.product.entity.Review;
import com.example.shoppingmall.domain.product.repository.ProductRepository;
import com.example.shoppingmall.domain.product.repository.ReviewRepository;
import com.example.shoppingmall.domain.user.entity.User;
import com.example.shoppingmall.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    /**
     * 리뷰 작성
     */
    @Transactional
    public Long writeReview(Long userId, Long productId, Integer rating, String content) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

        Review review = Review.builder()
                .user(user)
                .product(product)
                .rating(rating)
                .content(content)
                .build();

        reviewRepository.save(review);
        return review.getId();
    }

    /**
     * 특정 상품의 평점 높은 순 리뷰 조회
     */
    public List<Review> getReviewsByProduct(Long productId) {
        return reviewRepository.findByProductIdOrderByRatingDescIdDesc(productId);
    }
}