package com.example.shoppingmall.domain.product.controller;

import com.example.shoppingmall.domain.product.entity.Product;
import com.example.shoppingmall.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    /**
     * 상품 등록 API
     */
    @PostMapping("/new")
    public String create(@RequestBody Product product) {
        Long productId = productService.saveProduct(product);
        return "상품 등록 완료! 상품 고유 번호: " + productId;
    }

    /**
     * 상품 전체 목록 조회 API
     */
    @GetMapping
    public List<Product> list() {
        return productService.findProducts();
    }

    /**
     * 상품 상세 조회 API
     */
    @GetMapping("/{productId}")
    public Product detail(@PathVariable("productId") Long productId) {
        return productService.findOne(productId);
    }
}