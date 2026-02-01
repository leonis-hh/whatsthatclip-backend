package com.whatsthatclip.backend.service;

import com.whatsthatclip.backend.dto.FavoriteRequest;
import com.whatsthatclip.backend.entity.Favorite;
import com.whatsthatclip.backend.entity.SearchHistory;
import com.whatsthatclip.backend.entity.User;
import com.whatsthatclip.backend.repository.FavoriteRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FavoriteService {
    private FavoriteRepository favoriteRepository;

    public FavoriteService(FavoriteRepository favoriteRepository) {
        this.favoriteRepository = favoriteRepository;
    }

    public Favorite saveFavorite (FavoriteRequest request, User user) {
        Favorite favorite = new Favorite(request.getTitle(), request.getType(), request.getYear(), request.getOverview(), request.getPosterUrl(), LocalDateTime.now(), user);
        return favoriteRepository.save(favorite);
    }

    public List<Favorite> getFavoritesForUser (User user) {
        return favoriteRepository.findByUserOrderBySavedAtDesc(user);
    }
}
