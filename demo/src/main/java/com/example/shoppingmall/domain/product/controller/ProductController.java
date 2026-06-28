package com.example.shoppingmall.domain.product.controller;

// [의존성 모듈 인터페이스 정의] 외부 라이브러리 및 프로젝트 내부 상품 서비스 컴포넌트 임포트
import com.example.shoppingmall.domain.product.entity.Product;
import com.example.shoppingmall.domain.product.service.ProductService;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * [상품 도메인 제어 레이어 (Product Controller Module)]
 * 클라이언트 쇼핑몰 메인 화면(index.html) 및 상세 페이지의 인바운드 HTTP 요청을 수신하여
 * 상품 정보 적재 및 탐색 파이프라인(ProductService)으로 바인딩하는 웹 전송 계층 클래스입니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
// [CORS 크래시 방지 알고리즘] 프론트엔드 정적 웹 서버(Live Server: 5500 포트)의 상품 데이터 비동기 스캔 및 조회
// 요청을 공식 승인합니다.
@CrossOrigin(origins = "http://127.0.0.1:5500")
public class ProductController {

    // [단일 책임 원칙에 기반한 비즈니스 레이어 의존성 주입]
    private final ProductService productService;

    /**
     * [신규 피트니스 상품 등록 엔드포인트 API (관리자 백오피스 확장 대비)]
     * * @param requestDto 외부에서 수신한 상품 정보 데이터 전송 객체 (엔티티 격리)
     * 
     * @return 201 Created 상태 코드와 등록 완료 식별자 메시지 반환
     */
    @PostMapping("/new")
    public ResponseEntity<String> create(@RequestBody ProductSaveRequestDto requestDto) {
        // 1. [DTO -> Entity 변환 모듈화] 순수 데이터를 도메인 영속성 컨텍스트 계층 객체로 치환합니다.
        Product product = Product.builder()
                .name(requestDto.getName())
                .price(requestDto.getPrice())
                .stockQuantity(requestDto.getStockQuantity())
                .build();

        // 2. [서비스 레이어 호출] 도메인 서비스 서브루틴에 위임하여 상품을 등록하고 고유 키를 반환받습니다.
        Long productId = productService.saveProduct(product);

        // 3. [REST 스펙 동기화] 자원이 생성되었음을 뜻하는 표준 HTTP 상태 코드 201을 주입하여 응답합니다.
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("상품 등록이 정상적으로 완료되었습니다. ID: " + productId);
    }

    /**
     * [전체 상품 리스트 스캔 API (메인 쇼핑몰 진열대 렌더링용)]
     * * @return DB 내 가용 상품 목록을 DTO 형태로 정형화한 200 OK 응답 바디
     */
    @GetMapping
    public ResponseEntity<List<ProductResponseDto>> list() {
        // 1. [서비스 영속 데이터 리커버리] 영속성 컨텍스트에 잠재된 상품 목록을 스캔합니다.
        List<Product> products = productService.findProducts();

        // 2. [의존성 순환 참조 방지 알고리즘] 자바 Stream API 아키텍처를 가동하여 도메인 엔티티 군을 안전한 응답 전용 DTO 리스트로
        // 변환 및 캡슐화합니다.
        List<ProductResponseDto> responseDtos = products.stream()
                .map(ProductResponseDto::new)
                .collect(Collectors.toList());

        // 3. [최종 데이터 파싱] 정합성이 완성된 DTO 리스트 배열 데이터를 REST 규격 바디에 실어 전송합니다.
        return ResponseEntity.ok(responseDtos);
    }

    /**
     * [단건 상품 상세 검색 API (피트니스 기구 클릭 시 상세 레이아웃 팝업 연동)]
     * * @param productId 조회 타겟 상품 일련번호 (DB 식별용 주키)
     * 
     * @return 조회 성공 시 DTO 바디 반환 / 부재 시 Fail-Fast 400 Bad Request 에러 반환
     */
    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponseDto> detail(@PathVariable("productId") Long productId) {
        try {
            // 1. [도메인 추례 검증] 서비스 계층에서 엔티티 단건을 안전하게 반환받습니다. 부재 시 예외가 도출됩니다.
            Product product = productService.findOne(productId);

            // 2. [아키텍처 안전 격리] 획득한 엔티티를 출력 전용 응답 객체에 바인딩하여 변환 마감합니다.
            ProductResponseDto responseDto = new ProductResponseDto(product);

            return ResponseEntity.ok(responseDto);
        } catch (IllegalArgumentException e) {
            // 3. [비즈니스 예외 핸들링 분기] 유효하지 않은 식별자 접근 시 브라우저 오류 전파를 위해 400 에러 스펙을 주입합니다.
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    /**
     * [상품 적재 인바운드 전용 수신 데이터 캡슐화 DTO]
     */
    @Getter
    public static class ProductSaveRequestDto {
        private String name;
        private int price;
        private int stockQuantity;
    }

    /**
     * [아웃바운드 전송용 JSON 직렬화 전용 데이터 캡슐화 DTO]
     * 엔티티 클래스의 연관관계 내부 정보를 배제하여 런타임 직렬화 성능 최적화 및 보안 장벽을 형성합니다.
     */
    @Getter
    public static class ProductResponseDto {
        private final Long id;
        private final String name;
        private final int price;
        private final int stockQuantity;

        // [엔티티 결합 구조체 매핑 생성자]
        public ProductResponseDto(Product product) {
            this.id = product.getId();
            this.name = product.getName();
            this.price = product.getPrice();
            this.stockQuantity = product.getStockQuantity();
        }
    }
}