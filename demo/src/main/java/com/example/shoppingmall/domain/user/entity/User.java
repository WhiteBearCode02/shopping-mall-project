package com.example.shoppingmall.domain.user.entity;

// [의존성 모듈 인터페이스 정의] 객체-관계 매핑(ORM) 명세 준수 및 빌더 캡슐화를 위한 자카르타 퍼시스턴스 및 롬복 임포트
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * [회원 도메인 코어 엔티티 계층 (User Core Entity Module)]
 * 데이터베이스의 users 테이블과 매핑되며, 고객의 로그인 인증 메타데이터 및
 * 서비스 접근 권한(Role) 사양을 영속화하여 관리하는 핵심 도메인 모델 클래스입니다.
 */
@Entity
@Getter
@Table(name = "users") // [RDB 스키마 바인딩] 관계형 데이터베이스 회원 테이블 네임스페이스 매핑 동기화
// [무분별한 외부 객체 생성 제어] 영속성 프록시 계층 레이어의 정상 런타임 구동을 보장하되, 외부 레이어에서 인자 없는 new 객체 임의
// 생성을 PROTECTED로 격리 차단합니다.
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    // [기본키 식별 자원 정의] 관계형 데이터베이스의 시퀀스 자동 증가(IDENTITY) 메커니즘을 영속성 주키 알고리즘으로 동기화합니다.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    // [비즈니스 식별자 필드]
    // 로그인 계정 식별의 주키가 되는 이메일입니다. unique = true 제약조건을 가동하여 데이터베이스 커널 계층에서 중복 유입을
    // 차단합니다.
    @Column(nullable = false, unique = true)
    private String email;

    // [비즈니스 보안 필드] 향후 암호화 알고리즘 인코딩 서브루틴의 결과 해시 문자열이 상주하게 될 비밀번호 필드입니다.
    @Column(nullable = false)
    private String password;

    // [비즈니스 명세 필드] 쇼핑몰 서비스 및 배송 레이아웃에 마킹될 고객의 실명 데이터입니다.
    @Column(nullable = false)
    private String name;

    // [보안 인가 권한 필드]
    // EnumType.STRING 명세를 강제 적용하여 열거형 상수 텍스트 자체를 문자열로 데이터베이스 테이블에 박제합니다.
    // 이를 통해 향후 Enum 파일 중간에 새로운 권한 상수가 추가되더라도 순서 인덱싱 숫자가 밀려 데이터가 오염되는 현상을 원천 방어합니다.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /**
     * [엔티티 안전 생성을 위한 불변 객체 빌더 패턴 아키텍처 스펙]
     * 점진적 생성자 매개변수 누락 오류를 방지하고 생성 시점에 정합성이 확보된 완전한 회원 객체만 인스턴스화하도록 강제합니다.
     * 권한(Role) 인자가 외부 수신 DTO 단계에서 누락되어 전달될 경우를 대비해, 기본값으로 USER 등급을 주입하는 방어벽 알고리즘을
     * 수행합니다.
     */
    @Builder
    public User(String email, String password, String name, Role role) {
        this.email = email;
        this.password = password;
        this.name = name;
        // [방어벽 분기 알고리즘] 외부 바인딩 인자가 공백이거나 누락 시 기본 일반 회원 권한(USER)으로 격리 조율합니다.
        this.role = (role != null) ? role : Role.USER;
    }
}