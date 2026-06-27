package com.example.shoppingmall.domain.user.controller;

// [의존성 모듈 인터페이스 정의] 웹 프로토콜 통제 및 회원 비즈니스 레이어 연동을 위한 필수 라이브러리 및 서비스 임포트
import com.example.shoppingmall.domain.user.entity.User;
import com.example.shoppingmall.domain.user.service.UserService;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * [회원 도메인 제어 레이어 (User Controller Module)]
 * 쇼핑몰 서비스의 회원가입, 인증 등 계정 관련 프리젠테이션 요청을 수신하여
 * 회원 비즈니스 관리 트랜잭션(UserService)으로 라우팅 및 중재하는 웹 전송 계층 클래스입니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users") // [네임스페이스 동기화] 회원 도메인 하위 엔드포인트의 공통 URL 경로 바인딩
// [CORS 보안 해결] 프론트엔드 비동기 통신 자바스크립트 엔진(Live Server: 5500 포트)의 회원 요청 자격 증명을 공식
// 승인합니다.
@CrossOrigin(origins = "http://127.0.0.1:5500")
public class UserController {

    // [단일 책임 원칙에 기반한 비즈니스 레이어 의존성 주입]
    private final UserService userService;

    /**
     * [신규 회원가입 처리 REST API]
     * 사용자가 회원가입 정적 폼 페이지에서 입력한 계정 메타데이터를 파싱하여 영속성 계층에 안전하게 적재하는 엔드포인트입니다.
     * * @param requestDto 외부 유출 방지 장벽이 형성된 순수 회원가입 요청 전용 데이터 전송 객체 (DTO)
     * 
     * @return 가입 성공 시: 201 Created 상태 코드와 고유 번호 메시지 / 중복 가입 실패 시: 400 Bad Request와
     *         중복 예외 메시지
     */
    @PostMapping("/join")
    public ResponseEntity<String> signUp(@RequestBody UserJoinRequestDto requestDto) {
        try {
            // 1. [DTO -> Entity 변환 모듈화] 순수 요청 스펙 데이터를 도메인 영속성 컨텍스트 계층 객체로 치환 및 데이터 캡슐화를
            // 전개합니다.
            User user = User.builder()
                    .email(requestDto.getEmail())
                    .password(requestDto.getPassword())
                    .name(requestDto.getName())
                    .build();

            // 2. [비즈니스 서비스 모듈 호출] 도메인 서비스 서브루틴에 영속화 처리를 위임하고 발급된 고유 식별자 주키를 획득합니다.
            Long userId = userService.join(user);

            // 3. [REST 스펙 동기화] 자원이 성공적으로 생성되었음을 보장하기 위해 HTTP 표준 201 Created 코드를 파싱하여
            // 바인딩합니다.
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("회원가입 성공! 회원 고유 번호: " + userId);

        } catch (IllegalStateException e) {
            // 4. [Fail-Fast 비즈니스 중복 예외 핸들링] 이메일 중복 검증 알고리즘에 의해 거부되었을 때 예외 객체를 인터셉트합니다.
            // 5. [오류 응답 분기 동기화] 브라우저 자바스크립트가 에러 세션(.catch())으로 즉각 진입할 수 있도록 HTTP 400 상태 명세를
            // 주입하여 반환합니다.
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("회원가입 실패: " + e.getMessage());
        }
    }

    /**
     * [회원 적재 인바운드 전용 데이터 수신 캡슐화 DTO]
     * 도메인 코어 엔티티(User.java)를 프리젠테이션 웹 레이어로부터 완전히 격리하여 안전장벽을 구축하는 내부 정적 모듈 클래스입니다.
     */
    @Getter
    @NoArgsConstructor // [Jackson 직렬화 호환 알고리즘] 빈 생성자를 명시하여 JSON 파싱 인터셉터의 정상 구동을 보장합니다.
    public static class UserJoinRequestDto {
        // [이메일 계정 식별 필드] 로그인 및 중복 검증의 주 키가 되는 이메일 문자열
        private String email;

        // [비밀번호 보안 필드] 향후 암호화 알고리즘 인코딩 서브루틴의 대상이 되는 원시 패스워드
        private String password;

        // [사용자 닉네임 필드] 쇼핑몰 내 주문 및 메인 레이아웃에 노출될 고객 실명 메타데이터
        private String name;
    }
}