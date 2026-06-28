package com.example.shoppingmall.domain.user.repository;

// [의존성 모듈 인터페이스 정의] 유저 엔티티 및 스프링 데이터 JPA 코어 인프라 임포트
import com.example.shoppingmall.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * [회원 데이터 액세스 레이어 (User Repository Module)]
 * 데이터베이스의 User 테이블과 직접 통신하며 물리적인 쿼리(CRUD)를 수행하는 영속성 인터페이스입니다.
 * JpaRepository를 상속받음으로써 기본적인 쿼리 엔진은 스프링 부트가 런타임에 동적 프록시로 구현해 줍니다.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * [이메일 기반 단건 회원 조회 알고리즘]
     * 요구사항 명세: UserService에서 회원가입 시 중복된 이메일이 있는지 검증하기 위해 가동되는 쿼리 메서드입니다.
     * 작동 원리: 'findBy + Email' 네이밍 규칙을 파싱하여 스프링 데이터 JPA가 내부적으로 `SELECT * FROM user
     * WHERE email = ?` 쿼리를 자동 생성합니다.
     * * @param email 검증하고자 하는 고객의 이메일 문자열
     * 
     * @return 널 포인터 예외(NPE)를 방어하기 위한 자바 표준 Optional 래퍼 컨테이너 반환
     */
    Optional<User> findByEmail(String email);
}