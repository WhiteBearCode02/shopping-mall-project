package com.example.shoppingmall.domain.product.repository;

// [의존성 모듈 인터페이스 정의] 데이터 영속성 제어 및 리스트 컬렉션 바인딩을 위한 JPA 라이브러리와 엔티티 임포트
import com.example.shoppingmall.domain.product.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * [상품 리뷰 도메인 데이터 영속성 레이어 (Product Review Repository Module)]
 * 관계형 데이터베이스(RDB)의 리뷰(Reviews) 테이블과 매핑되어
 * 화면 진열대에 필요한 리뷰 스캔 및 정렬 조회 서브루틴을 제어하는 인프라 저장소 인터페이스입니다.
 */
@Repository
// [데이터 액세스 레이어 영속화] 스프링 컴포넌트 스캔 매커니즘이 이 인터페이스를 동적 프록시 빈으로 인스턴스화하도록 바인딩합니다.
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * [복합 인덱스 스캔 최적화 - 평점순 및 최신순 리뷰 목록 조회 알고리즘]
     * 요구사항 명세: 특정 피트니스 기구 상세 화면에 진입했을 때, 해당 상품에 등록된 리뷰 데이터를 평점 높은 순(1순위), 최신
     * 순(2순위)으로 정렬하여 리스트로 반환합니다.
     * 인프라 연동 포인트: Review 엔티티에 설계된 복합 인덱스(@Index)의 컬럼 순서(product_id, rating,
     * review_id DESC)와 쿼리 구조가 100% 일치합니다.
     * 결과적으로 데이터베이스 엔진이 별도의 가상 정렬 연산(Filesort)을 거치지 않고 B-Tree 인덱스만 타고 내려가 결과를 즉시
     * 파싱하는 최적화 서브루틴을 수행합니다.
     * * @param productId 리뷰를 추출하고자 하는 타겟 상품의 고유 일련번호 (외래키 매핑 타겟 인자)
     * 
     * @return 정합성 정렬 필터링 알고리즘을 통과하여 영속화된 Review 엔티티 컬렉션 리스트
     */
    List<Review> findByProductIdOrderByRatingDescIdDesc(Long productId);
}