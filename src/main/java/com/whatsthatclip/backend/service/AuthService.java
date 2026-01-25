package com.whatsthatclip.backend.service;

import com.whatsthatclip.backend.config.JwtUtil;
import com.whatsthatclip.backend.dto.AuthResponse;
import com.whatsthatclip.backend.entity.User;
import com.whatsthatclip.backend.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    private UserRepository userRepository;
    private JwtUtil jwtUtil;
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    public String signUp (String email, String password) {
        if (!userRepository.findByEmail(email).isPresent()) {
            User newUser = new User();
            newUser.setEmail(email);
            String hashedPassword = passwordEncoder.encode(password);
            newUser.setPassword(hashedPassword);
            userRepository.save(newUser);
            return "You have successfully signed up, you can now log in";
        } else {
            return "The email has already been used before, please log in to your existing account";
        }
    }

    public AuthResponse logIn(String email, String password) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        AuthResponse response= new AuthResponse();

        if (!optionalUser.isPresent()) {
            response.setToken(null);
            response.setMessage("Your login details are invalid, please try again\n");
            return response;
        }

        User user = optionalUser.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            response.setToken(null);
            response.setMessage("Your login details are invalid, please try again\n");
            return response;
        }

        String token = jwtUtil.generateToken(email);
        response.setToken(token);
        response.setMessage("Login successful");
        return response;
    }


    }


