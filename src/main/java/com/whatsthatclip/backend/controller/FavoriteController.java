package com.whatsthatclip.backend.controller;

import com.whatsthatclip.backend.dto.AnalyzeRequest;
import com.whatsthatclip.backend.dto.AnalyzeResponse;
import com.whatsthatclip.backend.dto.FavoriteRequest;
import com.whatsthatclip.backend.entity.Favorite;
import com.whatsthatclip.backend.entity.SearchHistory;
import com.whatsthatclip.backend.entity.User;
import com.whatsthatclip.backend.service.FavoriteService;
import com.whatsthatclip.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class FavoriteController {
    private FavoriteService favoriteService;
    public FavoriteController (FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }
    @PostMapping("/api/favorites")
    public ResponseEntity<?> saveFavorite (@RequestBody FavoriteRequest request) {
        try {
            return ResponseEntity.ok(favoriteService.saveFavorite(request));
        }  catch (Exception runTimeEx) {
            return ResponseEntity.status(409).body("Already in favorites");
        }
    }

    @GetMapping("/api/favorites")
    public List<Favorite> getFavoritesForUser () {
            return favoriteService.getFavoritesForUser();
    }
}
