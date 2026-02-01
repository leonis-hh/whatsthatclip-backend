package com.whatsthatclip.backend.repository;

import com.whatsthatclip.backend.entity.Favorite;
import com.whatsthatclip.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findByUserOrderBySavedAtDesc(User user);
}