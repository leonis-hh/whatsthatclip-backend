package com.whatsthatclip.backend.controller;

import com.whatsthatclip.backend.dto.AnalyzeRequest;
import com.whatsthatclip.backend.dto.AnalyzeResponse;
import com.whatsthatclip.backend.dto.FavoriteRequest;
import com.whatsthatclip.backend.entity.Favorite;
import com.whatsthatclip.backend.entity.SearchHistory;
import com.whatsthatclip.backend.entity.User;
import com.whatsthatclip.backend.service.FavoriteService;
import com.whatsthatclip.backend.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class FavoriteController {
    private FavoriteService favoriteService;
    private UserService userService;

    public FavoriteController (FavoriteService favoriteService, UserService userService) {
        this.favoriteService = favoriteService;
        this.userService = userService;
    }
    @PostMapping("/api/favorites")
    public Favorite saveFavorite (@RequestBody FavoriteRequest request) {
        User currentUser = userService.getCurrentUser();
        return favoriteService.saveFavorite(request, currentUser);
    }

    @GetMapping("/api/favorites")
    public List<Favorite> getFavoritesForUser () {
            User currentUser = userService.getCurrentUser();
            return favoriteService.getFavoritesForUser(currentUser);
    }
}
