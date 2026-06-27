package com.example.shoppingmall.global.exception;

// [의존성 모듈 인터페이스 정의] JSON 데이터 전송 객체 필드 자동 추출을 위한 롬복 라이브러리 임포트
import lombok.Getter;

/**
 * [전역 에러 응답 공통 규격 레이어 (Global Error Response Format Module)]
 * 시스템 내부 비즈니스 로직(예: 재고 부족, 이메일 중복 등)에서 런타임 예외가 유발되었을 때,
 * 클라이언트 웹 브라우저(index.html 프리젠테이션 계층)에게 일관된 형태의 HTTP 실패 메타데이터를 파싱하여 제공하는 데이터 구조체
 * 클래스입니다.
 */
@Getter // [JSON 직렬화 링킹] 스프링 내부의 Jackson 라이브러리가 본 객체를 JSON 텍스트 문자열로 역직렬화할 때 Getter 서브루틴을
        // 추론 가동합니다.
public class ErrorResponse {

    // [보안 및 불변성 필드]
    // 예외 발생 시 도출된 원인 사유가 담기는 문자열 메시지 필드입니다.
    // 멀티스레드 서블릿 컨테이너 환경에서 에러 데이터가 중간에 위변조되는 크래시를 막기 위해 final 불변 캡슐화를 강제합니다.
    private final String message;

    // [HTTP 프로토콜 명세 필드] 클라이언트가 자바스크립트 비동기 오류 트랙(.catch())에서 에러 분기를 인지하도록 돕는 HTTP 상태
    // 코드 값 (예: 400, 404, 500)
    private final int status;

    /**
     * [에러 응답 객체 정형화 생성자 아키텍처 스펙]
     * final 불변 필드에 무결한 에러 메시지와 상태 코드를 주입하여 원자적(Atomic) 데이터 전송 객체를 인스턴스화합니다.
     * * @param message 비즈니스 레이어 또는 인프라 레이어에서 전파된 핵심 예외 원인 문구
     * 
     * @param status HTTP 표준 규격을 명시하는 상태 코드 번호
     */
    public ErrorResponse(String message, int status) {
        this.message = message;
        this.status = status;
    }
}