package com.example.shoppingmall.domain.user.service;

import com.example.shoppingmall.domain.user.entity.User;
import com.example.shoppingmall.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본적으로 조회 성능을 최적화합니다.
public class UserService {

    private final UserRepository userRepository;

    /**
     * 회원가입 기능
     */
    @Transactional // 데이터 변경이 일어나므로 쓰기 권한 트랜잭션을 부여합니다.
    public Long join(User user) {
        // [추론 과정] 중복 회원 검증 알고리즘을 먼저 거쳐 안정성을 확보합니다.
        validateDuplicateUser(user);

        userRepository.save(user);
        return user.getId();
    }

    /**
     * 중복 회원 검증 (이메일 기준)
     */
    private void validateDuplicateUser(User user) {
        userRepository.findByEmail(user.getEmail())
                .ifPresent(m -> {
                    throw new IllegalStateException("이미 존재하는 이메일입니다.");
                });
    }
}