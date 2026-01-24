package com.whatsthatclip.backend.service;

import com.whatsthatclip.backend.entity.User;
import com.whatsthatclip.backend.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    private UserRepository userRepository;
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
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

    public String logIn(String email, String password) {
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (!optionalUser.isPresent()) {
            return "Your login details are invalid, please try again";
        }

        User user = optionalUser.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return "Your login details are invalid, please try again";
        }

        return "Login successful";
    }


    }


