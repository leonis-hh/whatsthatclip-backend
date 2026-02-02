package com.whatsthatclip.backend.service;

import com.whatsthatclip.backend.config.JwtUtil;
import com.whatsthatclip.backend.dto.UserProfileResponse;
import com.whatsthatclip.backend.entity.User;
import com.whatsthatclip.backend.repository.FavoriteRepository;
import com.whatsthatclip.backend.repository.SearchHistoryRepository;
import com.whatsthatclip.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private JwtUtil jwtUtil;
    private UserRepository userRepository;
    private FavoriteRepository favoriteRepository;
    private SearchHistoryRepository searchRepository;

    public UserService (JwtUtil jwtUtil, UserRepository userRepository, FavoriteRepository favoriteRepository, SearchHistoryRepository searchRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.favoriteRepository = favoriteRepository;
        this.searchRepository = searchRepository;
    }

    public User getCurrentUser() {
        String email = jwtUtil.getCurrentUserEmail();
        if (email == null) {
            return null;
        }
        return userRepository.findByEmail(email).orElse(null);
    }

    public UserProfileResponse getUserProfile () {
        User user = getCurrentUser();
        UserProfileResponse response = new UserProfileResponse();
        response.setEmail(user.getEmail());
        response.setTotalSearches(searchRepository.countByUser(user));
        response.setMoviesIdentified(searchRepository.countByUserAndType(user, "Movie"));
        response.setTvShowsIdentified(searchRepository.countByUserAndType(user, "TV Show"));
        response.setTotalFavorites(favoriteRepository.countByUser(user));
        response.setMovieFavorites(favoriteRepository.countByUserAndType(user, "Movie"));
        response.setTvShowFavorites(favoriteRepository.countByUserAndType(user, "TV Show"));
        return response;
    }

}
