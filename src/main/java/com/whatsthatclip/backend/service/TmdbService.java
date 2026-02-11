package com.whatsthatclip.backend.service;

import com.whatsthatclip.backend.tmdb.TmdbSearchResponse;
import com.whatsthatclip.backend.tmdb.TmdbTvSearchResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;


import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class TmdbService {
    @Value("${tmdb.api.key}")
    private String apiKey;
    private RestTemplate restTemplate = new RestTemplate();

    public TmdbSearchResponse searchMovie(String query) {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = "https://api.themoviedb.org/3/search/movie?api_key=" + apiKey + "&query=" + encodedQuery;
        return restTemplate.getForObject(url, TmdbSearchResponse.class);
    }

    public TmdbTvSearchResponse searchTv(String query) {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = "https://api.themoviedb.org/3/search/tv?api_key=" + apiKey + "&query=" + encodedQuery;
        return restTemplate.getForObject(url, TmdbTvSearchResponse.class);
    }

    public Map<String, Object> getWatchProviders(Integer id, String type, String country) {
        String url;
        if (type.equals("movie")) {
            url = "https://api.themoviedb.org/3/movie/" + id + "/watch/providers?api_key=" + apiKey;
        } else {
            url = "https://api.themoviedb.org/3/tv/" + id + "/watch/providers?api_key=" + apiKey;
        }
        System.out.println("Fetching watch providers for " + type + " " + id + " country: " + country);

        String response = restTemplate.getForObject(url, String.class);

        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> fullResponse = mapper.readValue(response, Map.class);
            Map<String, Object> results = (Map<String, Object>) fullResponse.get("results");
            Map<String, Object> countryData = (Map<String, Object>) results.get(country);

            if (countryData == null) {
                return Map.of("message", "No providers found for country: " + country);
            }

            return countryData;
        } catch (Exception e) {
            return Map.of("message", "Error parsing watch providers");
        }
    }



}
