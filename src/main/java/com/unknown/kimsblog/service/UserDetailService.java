package com.unknown.kimsblog.service;

import com.unknown.kimsblog.model.User;
import com.unknown.kimsblog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException; // 추가
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserDetailService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public User loadUserByUsername(String email){
        System.out.println("=== UserDetailService.loadUserByUsername ===");
        System.out.println("Looking for user with email: " + email);

        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    System.out.println("User not found: " + email);
                    return new UsernameNotFoundException("User not found: " + email); // 변경
                });
    }
}