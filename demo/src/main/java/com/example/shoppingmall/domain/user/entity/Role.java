package com.example.shoppingmall.domain.user.entity;

/**
 * [회원 권한 도메인 타입 상수 레이어 (User Role Enum Module)]
 * 쇼핑몰 서비스에 가입한 사용자 계정의 접근 권한 식별자를 정의하며,
 * 비즈니스 로직 및 API 엔드포인트의 인프라 보안 인가(Authorization) 처리를 중재하는 열거형(Enum) 클래스입니다.
 */
public enum Role {

    // [일반 고객 권한 상수 정의] 상품 조회, 장바구니 적재, 주문 결제 및 비관적 락 트랜잭션 수립이 승인된 표준 사용자 레이어
    USER,

    // [시스템 관리자 권한 상수 정의] 백오피스 상품 적재(/api/products/new), 재고 보충, 회원 블랙리스트 관리가 승인된 최고
    // 운영자 레이어
    ADMIN
}