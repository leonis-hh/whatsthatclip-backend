package com.whatsthatclip.backend.controller;

import com.whatsthatclip.backend.dto.UserProfileResponse;
import com.whatsthatclip.backend.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    private UserService userService;

    public UserController (UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/api/user/profile")
    public UserProfileResponse getProfile() {
        return userService.getUserProfile();
    }
}
