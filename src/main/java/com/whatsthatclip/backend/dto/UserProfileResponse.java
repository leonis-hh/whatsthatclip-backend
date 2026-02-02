package com.whatsthatclip.backend.dto;

import lombok.Data;
import lombok.Getter;

@Data
public class UserProfileResponse {
    private String email;
    private Long totalSearches;
    private Long moviesIdentified;
    private Long tvShowsIdentified;
    private Long totalFavorites;
    private Long movieFavorites;
    private Long tvShowFavorites;
}
