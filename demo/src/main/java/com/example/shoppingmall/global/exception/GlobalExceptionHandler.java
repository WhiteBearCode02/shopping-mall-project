package com.example.shoppingmall.global.exception;

// [의존성 모듈 인터페이스 정의] 전역 예외 캡처 및 HTTP 응답 규격 정형화를 위한 스프링 인프라 라이브러리 임포트
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * [전역 에러 가로채기 인터셉터 레이어 (Global Exception Handler Module)]
 * 애플리케이션 내부의 모든 컨트롤러 계층을 사방에서 감싸 안는 Spring AOP 방어벽입니다.
 * 비즈니스 로직(Service, Entity) 실행 도중 터져 나오는 런타임 익셉션을 중앙 통제실에서 캡처하여,
 * 사전에 정의된 공통 ErrorResponse JSON 포맷으로 직렬화 및 정형화하여 클라이언트에 응답하는 역할을 수행합니다.
 */
@RestControllerAdvice
// [AOP 관점 지향 컴포넌트 바인딩] 프로젝트 내부 전역의 @RestController 계층에서 발생하는 예외 핸들링 파이프라인을 이
// 클래스 모듈로 일괄 집약시킵니다.
public class GlobalExceptionHandler {

    /**
     * [비즈니스 예외 인터셉트 및 클라이언트 전파 서브루틴]
     * 요구사항 명세: `Product` 내부의 재고 부족 예외 또는 `UserService` 내부의 중복 회원 예외 발생 시 구동됩니다.
     * 작동 원리: 런타임에 지정된 두 종류의 예외가 던져지면 스프링 예외 처리 엔진이 이를 가로채 본 서브루틴의 인자로 주입합니다.
     * * @param ex 비즈니스 로직 단에서 사유 문자열을 품고 전파된 실시간 RuntimeException 객체
     * 
     * @return 클라이언트 브라우저가 예외 분기 추론을 수행할 수 있도록 400 Bad Request 상태 코드 스펙과 매핑 완료된
     *         ErrorResponse 구조체
     */
    @ExceptionHandler({ IllegalArgumentException.class, IllegalStateException.class }) // [타겟 익셉션 그룹 바인딩] 잘못된 인자 및 상태 오염
                                                                                       // 예외를 타겟팅합니다.
    public ResponseEntity<ErrorResponse> handleBadRequestException(RuntimeException ex) {

        // 1. [에러 응답 규격 정형화 인스턴스화] 던져진 원시 예외 메시지와 HTTP 400 상태 코드를 바인딩하여 불변 객체로 조립합니다.
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST.value());

        // 2. [REST 응답 프로토콜 반환] 웹 표준 규격 ResponseEntity 컨테이너에 실어 브라우저 자바스크립트 엔진으로 최종 전송
        // 마감합니다.
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * [최종 시스템 예기치 못한 인프라 내부 오류 방어벽 서브루틴]
     * 요구사항 명세: 데이터베이스 다운, 네트워크 단절, NullPointerException 등 개발자가 미처 예측하지 못한 치명적인 로우
     * 레벨 에러를 통제합니다.
     * 보안 원칙: 원시 에러의 스택 트레이스나 구체적인 쿼리 문법 메시지를 그대로 노출하면 인프라가 유출되므로, "서버 내부 오류"로 마스킹
     * 캡슐화를 전개합니다.
     * * @param ex 시스템 코어 계층에서 최상위 루트로 터져 나온 Exception 원형 객체
     * 
     * @return 외부 해커의 추론을 차단하는 마스킹 메시지와 함께 HTTP 500 상태 스펙을 바인딩하여 안전 반환
     */
    @ExceptionHandler(Exception.class) // [루트 익셉션 가치 인터셉트] 하위 필터링에서 걸러지지 않은 모든 미지의 예외를 최종 수집하는 폴백(Fallback) 방어선입니다.
    public ResponseEntity<ErrorResponse> handleAllException(Exception ex) {

        // 1. [보안 마스킹 데이터 직렬화] 원시 오류 사유를 철저히 격리 은닉하고 정돈된 공통 메시지와 HTTP 500 명세를 주입합니다.
        ErrorResponse errorResponse = new ErrorResponse("서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR.value());

        // 2. [최종 인프라 래치 반환] 데이터베이스 정합성이 깨진 비상 상황임을 브라우저에 알리는 HTTP 500 에러 스펙을 주입하여
        // 반환합니다.
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}