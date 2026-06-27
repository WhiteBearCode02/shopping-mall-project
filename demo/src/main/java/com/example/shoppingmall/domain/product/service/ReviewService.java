package com.example.shoppingmall.domain.product.service;

// [의존성 모듈 인터페이스 정의] 리뷰 비즈니스 트랜잭션 구동을 위한 도메인 엔티티 및 계층별 데이터 저장소 레이어 임포트
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

/**
 * [상품 리뷰 도메인 코어 비즈니스 로직 레이어 (Product Review Service Module)]
 * 피트니스 용품에 대한 평점 및 후기 데이터를 데이터베이스 테이블에 영속화하고,
 * 설계된 복합 인덱싱 조건을 기반으로 최적화된 정렬 목록을 추출 및 중재하는 서비스 컴포넌트입니다.
 */
@Service
@RequiredArgsConstructor
// [조회 트랜잭션 격리 최적화] 클래스 레벨에 readOnly = true 명세를 주입하여 불필요한 플러시 연산을 생략, 리뷰 전건 조회
// 성능을 최적화합니다.
@Transactional(readOnly = true)
public class ReviewService {

    // [단일 책임 원칙에 기반한 다중 도메인 저장소 컴포넌트 의존성 주입]
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    /**
     * [신규 회원 리뷰 등록 및 영속화 알고리즘]
     * 요구사항 명세: 특정 회원이 구매한 상품에 대해 별점과 텍스트를 입력하면 무결성을 검증한 뒤 테이블에 적재합니다.
     * * @param userId 리뷰 작성을 요청한 고객 고유 일련번호
     * 
     * @param productId 대상 피트니스 용품 고유 일련번호
     * @param rating    사용자가 부여한 평점 점수 스펙 (1 ~ 5점 바운더리 제어 대상)
     * @param content   사용자가 텍스트 폼에 입력한 리뷰 후기 본문 문자열
     * @return 영속성 컨텍스트 시퀀스 메커니즘에 의해 발급 완료된 리뷰 고유 식별 주키 (Review ID)
     */
    @Transactional // [쓰기 트랜잭션 전파 오버라이딩] 쓰기 격리 수준을 활성화하여 관계형 데이터베이스 테이블에 실제 INSERT 커밋 래치를 구동합니다.
    public Long writeReview(Long userId, Long productId, Integer rating, String content) {

        // 1. [회원 존재 유무 유효성 검증] 영속성 컨텍스트에서 유저 정보를 탐색하고, 부재 시 Fail-Fast 메커니즘에 따라 즉시 예외를
        // 터트립니다.
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 2. [상품 존재 유무 유효성 검증] 대상 피트니스 용품 정보를 탐색하고, 부재 시 하위 스레드로 오염 데이터가 흐르지 않게 차단합니다.
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

        // 3. [엔티티 안전 생성을 위한 빌더 패턴 아키텍처 가동] 도메인 모델 계층의 데이터 규격에 맞게 뼈대 객체를 빌딩합니다.
        Review review = Review.builder()
                .user(user)
                .product(product)
                .rating(rating)
                .content(content)
                .build();

        // 4. [영속성 가짜 저장 실행] 영속성 컨텍스트 내부 1차 캐시에 엔티티를 적재하고, 트랜잭션 종료 시점에 디스크 테이블로 영속화합니다.
        reviewRepository.save(review);

        return review.getId();
    }

    /**
     * [복합 인덱스 동기화 기반 평점순/최신순 리뷰 목록 스캔 서브루틴]
     * 요구사항 명세: 사용자가 상품 상세 레이아웃을 열었을 때, 리뷰 진열대를 평점 높은 순과 최신 식별자 순으로 필터링하여 출력합니다.
     * 인프라 최적화: Review 엔티티에 세팅된 복합 인덱스(idx_product_rating_id)를 그대로 타고 내려가 무거운 디스크 소팅
     * 연산 없이 쿼리를 마감합니다.
     * * @param productId 리뷰 목록을 스캔하고자 하는 기준 타겟 상품 일련번호 (외래키 조회 조건 인자)
     * 
     * @return 정합성 정렬 필터링 서브루틴을 통과하여 영속화된 Review 엔티티 컬렉션 리스트
     */
    public List<Review> getReviewsByProduct(Long productId) {
        // 1. [레포지토리 쿼리 메서드 위임] 동적 프록시가 구현한 커스텀 정렬 메서드를 호출하여 결과셋을 복원합니다.
        return reviewRepository.findByProductIdOrderByRatingDescIdDesc(productId);
    }
}