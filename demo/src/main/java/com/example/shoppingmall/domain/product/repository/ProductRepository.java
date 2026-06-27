package com.example.shoppingmall.domain.product.repository;

import com.example.shoppingmall.domain.product.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
// [데이터 액세스 레이어 영속화] 스프링 데이터 JPA 인프라가 이 인터페이스를 동적 프록시 빈으로 인스턴스화하도록 마킹합니다.
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * 동시성 제어를 위해 비관적 쓰기 락(Pessimistic Write Lock)을 거는 조회 메서드
     * DB에서 SELECT ... FOR UPDATE 구문이 실행됩니다.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdWithPessimisticLock(@Param("id") Long id);
}