package com.example.shoppingmall.domain.user.controller;

import com.example.shoppingmall.domain.user.entity.User;
import com.example.shoppingmall.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users") // 공통 URL 경로 설정
public class UserController {

    private final UserService userService;

    /**
     * 회원가입 API
     */
    @PostMapping("/join")
    public String signUp(@RequestBody User user) {
        try {
            Long userId = userService.join(user);
            return "회원가입 성공! 회원 고유 번호: " + userId;
        } catch (IllegalStateException e) {
            // 중복 회원 발생 시 에러 메시지 반환
            return e.getMessage();
        }
    }
}