package com.whatsthatclip.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FavoriteRequest {
    private String title;
    private String type;
    private String year;
    private String overview;
    private String posterUrl;
}
