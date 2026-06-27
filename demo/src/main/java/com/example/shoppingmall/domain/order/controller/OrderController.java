package com.example.shoppingmall.domain.order.controller;

// [의존성 모듈 인터페이스 정의] 외부 라이브러리 및 프로젝트 내부 도메인 서비스 컴포넌트 임포트
import com.example.shoppingmall.domain.order.service.OrderService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * [주문 도메인 제어 레이어 (Order Controller Module)]
 * 클라이언트(웹 브라우저 프리젠테이션 계층)의 HTTP 비동기 요청을 수신하여
 * 백엔드 코어 비즈니스 트랜잭션(OrderService)으로 라우팅하는 진입점 게이트웨이 클래스입니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
// [CORS 크래시 방지 알고리즘] 프론트엔드 정적 웹 서버(Live Server: 5500 포트)의 교차 출처 자원 공유 요청을 공식
// 승인합니다.
@CrossOrigin(origins = "http://127.0.0.1:5500")
public class OrderController {

    // [의존성 주입 코어] 단일 책임 원칙(SRP)에 따라 주문 비즈니스 로직 처리를 전담하는 서비스 인스턴스 캡슐화
    private final OrderService orderService;

    /**
     * [주문 생성 처리 비동기 인바운드 엔드포인트 API]
     * 사용자가 화면에서 '바로 주문하기' 버튼을 누르면 자바스크립트 fetch() 통신을 통해 호출되는 REST API 메서드입니다.
     * * @param request 클라이언트가 JSON 형태로 전송한 사용자 고유 식별자(userId), 상품 고유
     * 식별자(productId), 구매 수량(count) 맵핑 객체
     * 
     * @return 트랜잭션 성공 시: 200 OK 상태 코드와 생성된 주문 ID / 실패 시: 400 Bad Request 상태 코드와
     *         비즈니스 예외 메시지
     */
    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody OrderRequestDto request) {
        try {
            // 1. [서비스 레이어 모듈 호출] 주입된 오더 서비스의 order 서브루틴을 구동하여 비관적 락 트랜잭션을 실행합니다.
            Long orderId = orderService.order(request.getUserId(), request.getProductId(), request.getCount());

            // 2. [정상 응답 파이프라인 정형화] 주문 생성이 성공하면 데이터 정합성이 확보된 상태이므로 HTTP 200 상태 명세와 결과 바디를
            // 반환합니다.
            return ResponseEntity.ok("주문이 성공적으로 완료되었습니다. 주문 번호: " + orderId);

        } catch (IllegalArgumentException e) {
            // 3. [Fail-Fast 비즈니스 예외 예방 처리] 재고 부족 및 유효하지 않은 바인딩 인자 발생 시 예외 객체 메시지를 캡처합니다.
            // 4. [오류 응답 분기 동기화] 브라우저 자바스크립트 엔진이 예외 트랙(`.catch()`)으로 즉시 진입할 수 있도록 HTTP 400
            // 에러 스펙을 주입하여 반환합니다.
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("주문 실패: " + e.getMessage());
        }
    }

    /**
     * [네트워크 통신 계층 데이터 전송 객체 모듈 (Data Transfer Object)]
     * HTTP Request Body에 실려오는 원시 JSON 데이터를 자바 런타임 객체 구조로 역직렬화(Deserialization)하기 위한
     * 데이터 캡슐화 껍데기 클래스입니다.
     */
    @Getter
    public static class OrderRequestDto {
        // [사용자 바인딩 필드] 주문을 요청한 회원 고유 일련번호 (DB 외래키 매핑 타겟)
        private Long userId;

        // [상품 바인딩 필드] 구매 타겟이 되는 피트니스 용품 고유 일련번호 (비관적 락 조회 키)
        private Long productId;

        // [수량 바인딩 필드] 사용자가 선택한 구매 요청 개수 (재고 차감 검증 알고리즘 대상 인자)
        private int count;
    }
}