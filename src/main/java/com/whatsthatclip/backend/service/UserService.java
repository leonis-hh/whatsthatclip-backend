package com.whatsthatclip.backend.service;

import com.whatsthatclip.backend.config.JwtUtil;
import com.whatsthatclip.backend.entity.User;
import com.whatsthatclip.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private JwtUtil jwtUtil;
    private UserRepository userRepository;

    public UserService (JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        String email = jwtUtil.getCurrentUserEmail();
        if (email == null) {
            return null;
        }
        return userRepository.findByEmail(email).orElse(null);
    }

}
