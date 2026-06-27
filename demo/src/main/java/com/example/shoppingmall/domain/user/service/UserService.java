package com.example.shoppingmall.domain.user.service;

// [의존성 모듈 인터페이스 정의] 회원 비즈니스 트랜잭션 구동을 위한 도메인 엔티티 및 영속성 데이터 저장소 레이어 임포트
import com.example.shoppingmall.domain.user.entity.User;
import com.example.shoppingmall.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * [회원 도메인 코어 비즈니스 로직 레이어 (User Service Module)]
 * 쇼핑몰 서비스의 회원가입 처리, 이메일 식별자 기반의 중복 계정 유효성 검증을 전담하며,
 * 영속성 컨텍스트 계층과 상호작용하여 회원 테이블의 비즈니스 트랜잭션을 제어하는 클래스입니다.
 */
@Service
@RequiredArgsConstructor
// [조회 트랜잭션 격리 최적화] 클래스 레벨에 readOnly = true 명세를 주입하여 불필요한 스냅샷 생성 및 플러시 연산을 생략,
// 전반적인 조회 성능을 최적화합니다.
@Transactional(readOnly = true)
public class UserService {

    // [단일 책임 원칙에 기반한 데이터 액세스 레이어 의존성 주입]
    private final UserRepository userRepository;

    /**
     * [신규 회원 가입 및 영속화 알고리즘]
     * 요구사항 명세: 외부 계층으로부터 전달받은 회원 객체의 메타데이터 정합성을 검증한 뒤 데이터베이스 테이블에 안전하게 적재합니다.
     * * @param user 외부 입력 DTO를 거쳐 인스턴스화 완료된 회원 도메인 엔티티 객체
     * 
     * @return 영속성 시퀀스 메커니즘에 의해 발급 완료된 회원 고유 식별 주키 (User ID)
     */
    @Transactional // [쓰기 트랜잭션 전파 오버라이딩] 쓰기 격리 수준을 활성화하여 관계형 데이터베이스 테이블에 실제 INSERT 커밋 래치를 구동합니다.
    public Long join(User user) {
        // 1. [Fail-Fast 선제 유효성 검증] 중복 회원 검증 알고리즘을 먼저 거쳐 이메일 자원의 정합성과 안전성을 확보합니다.
        validateDuplicateUser(user);

        // 2. [저장 메커니즘 가동] 리포지토리의 save 서브루틴을 호출하여 엔티티를 영속성 컨텍스트 1차 캐시에 바인딩합니다.
        userRepository.save(user);

        // 3. [식별자 주키 반환] 데이터베이스 시퀀스 발급 메커니즘을 통해 복원 완료된 유일 키값을 서비스 호출 계층으로 전달합니다.
        return user.getId();
    }

    /**
     * [이메일 식별자 기준 중복 가입 방어벽 검증 서브루틴]
     * 아키텍처 원칙: 영속성 저장소를 스캔하여 동일한 이메일을 선점한 기존 레코드가 있는지 판별합니다.
     * 찰나의 순간에 발생하는 동시성 충돌은 DB 테이블의 유니크 제약조건(Unique Constraint)이 최종 방어합니다.
     * * @param user 중복 검사를 진행하고자 하는 대상 회원 데이터 인스턴스
     */
    private void validateDuplicateUser(User user) {
        // 1. 데이터 저장소에 매핑된 커텀 쿼리 메서드 findByEmail을 가동하여 자바 표준 Optional 컨테이너 객체를 확보합니다.
        userRepository.findByEmail(user.getEmail())
                // 2. 만약 Optional 내부 객체 내용물이 존재할(ifPresent) 경우, 즉시 데이터 오염으로 판단하여 분기 서브루틴을 전개합니다.
                .ifPresent(m -> {
                    // 3. 예외 발생 시 현재 스레드 라인의 트랜잭션을 즉각 마비시키고 비즈니스 커밋을 원천 롤백으로 전환합니다.
                    throw new IllegalStateException("이미 존재하는 이메일입니다.");
                });
    }
}