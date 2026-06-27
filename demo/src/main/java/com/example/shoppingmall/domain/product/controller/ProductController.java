package com.example.shoppingmall.domain.product.controller;

import com.example.shoppingmall.domain.product.entity.Product;
import com.example.shoppingmall.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
// [CORS 보안 해결] Live Server 정적 자원 기본 포트인 5500번 도메인의 자원 공유를 승인합니다.
@CrossOrigin(origins = "http://127.0.0.1:5500")
public class ProductController {

    private final ProductService productService;

    /**
     * 신규 상품 등록 API (관리자 운영 기능 확장 대비)
     * * @param product 등록할 상품 엔티티 객체
     * 
     * @return 등록 완료 메시지 및 고유 식별자
     */
    @PostMapping("/new")
    public String create(@RequestBody Product product) {
        Long productId = productService.saveProduct(product);
        return "상품 등록이 정상적으로 완료되었습니다. ID: " + productId;
    }

    /**
     * 상품 전체 목록 조회 API (프론트엔드 index.html 연동 대상)
     * * @return DB에 적재된 전체 상품 JSON 리스트 데이터
     */
    @GetMapping
    public List<Product> list() {
        // 지연 로딩 최적화를 유지하며 전체 리스트를 반환합니다.
        return productService.findProducts();
    }

    /**
     * 상품 단건 상세 조회 API (상세 페이지 정보 제공용)
     * * @param productId 조회 타겟 상품 일련번호
     * 
     * @return 검증 알고리즘을 통과한 단건 엔티티 객체
     */
    @GetMapping("/{productId}")
    public Product detail(@PathVariable("productId") Long productId) {
        // Fail-Fast 메커니즘에 의해 없는 ID 조회 시 즉시 400 Bad Request 계열 예외를 발생시킵니다.
        return productService.findOne(productId);
    }
}