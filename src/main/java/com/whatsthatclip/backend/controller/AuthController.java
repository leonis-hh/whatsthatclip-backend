package com.whatsthatclip.backend.controller;

import com.whatsthatclip.backend.dto.AuthRequest;
import com.whatsthatclip.backend.dto.AuthResponse;
import com.whatsthatclip.backend.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth")
public class AuthController {
    private AuthService service;

    public AuthController (AuthService service) {
        this.service = service;
    }
    @PostMapping("/signup")
    public String signUp (@RequestBody AuthRequest request) {
        return service.signUp(request.getEmail(), request.getPassword());
    }

    @PostMapping("/login")
    public AuthResponse logIn (@RequestBody AuthRequest request) {
        return service.logIn(request.getEmail(), request.getPassword());
    }

}
