package com.whatsthatclip.backend.service;

import com.whatsthatclip.backend.dto.AnalyzeRequest;
import com.whatsthatclip.backend.dto.AnalyzeResponse;
import com.whatsthatclip.backend.entity.SearchHistory;
import com.whatsthatclip.backend.repository.SearchHistoryRepository;
import com.whatsthatclip.backend.tmdb.TmdbMovieResult;
import com.whatsthatclip.backend.tmdb.TmdbSearchResponse;
import com.whatsthatclip.backend.tmdb.TmdbTvResult;
import com.whatsthatclip.backend.tmdb.TmdbTvSearchResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AnalyzeService {
    private SearchHistoryRepository repository;
    private RestTemplate restTemplate = new RestTemplate();

    @Value("${tmdb.api.key}")
    private String apiKey;

    public AnalyzeService(SearchHistoryRepository repository) {
        this.repository = repository;
    }

    public AnalyzeResponse analyze(AnalyzeRequest request) {
        String videoUrl = request.getVideoUrl();
        TmdbSearchResponse movieResults = searchMovie(videoUrl);
        TmdbTvSearchResponse tvResults = searchTv(videoUrl);

        TmdbMovieResult movie = getTopMovie(movieResults);
        TmdbTvResult tv = getTopTv(tvResults);

        if (movie == null && tv == null) {
            AnalyzeResponse response = new AnalyzeResponse();
            response.setMessage("No results found");
            return response;
        }

        String title, type, date, overview, posterPath;

        if (movie != null && (tv == null || movie.getPopularity() > tv.getPopularity())) {
            title = movie.getTitle();
            type = "Movie";
            date = movie.getRelease_date();
            overview = movie.getOverview();
            posterPath = movie.getPoster_path();
        } else {
            title = tv.getName();
            type = "TV Show";
            date = tv.getFirst_air_date();
            overview = tv.getOverview();
            posterPath = tv.getPoster_path();
        }

        return buildFinalResponse(title, type, date, overview, posterPath, videoUrl);
    }

    private TmdbMovieResult getTopMovie(TmdbSearchResponse response) {
        if (response != null && response.getResults() != null && !response.getResults().isEmpty()) {
            return response.getResults().get(0);
        }
        return null;
    }

    private TmdbTvResult getTopTv(TmdbTvSearchResponse response) {
        if (response != null && response.getResults() != null && !response.getResults().isEmpty()) {
            return response.getResults().get(0);
        }
        return null;
    }

    private AnalyzeResponse buildFinalResponse(String title, String type, String date, String overview, String posterPath, String videoUrl) {
        String year = (date != null && date.length() >= 4) ? date.substring(0, 4) : "N/A";
        String posterUrl = "https://image.tmdb.org/t/p/w500" + posterPath;

        AnalyzeResponse response = new AnalyzeResponse();
        response.setTitle(title);
        response.setType(type);
        response.setYear(year);
        response.setOverview(overview);
        response.setPosterUrl(posterUrl);
        response.setMessage("Received URL: " + videoUrl);

        saveSearch(videoUrl, title, type, year, overview, posterUrl);

        return response;
    }

    public SearchHistory saveSearch(String videoUrl, String title, String type, String year, String overview, String posterUrl) {
        SearchHistory search = new SearchHistory(videoUrl, title, type, year, overview, posterUrl, LocalDateTime.now());
        return repository.save(search);
    }

    private TmdbSearchResponse searchMovie(String query) {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = "https://api.themoviedb.org/3/search/movie?api_key=" + apiKey + "&query=" + encodedQuery;
        return restTemplate.getForObject(url, TmdbSearchResponse.class);
    }

    private TmdbTvSearchResponse searchTv(String query) {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = "https://api.themoviedb.org/3/search/tv?api_key=" + apiKey + "&query=" + encodedQuery;
        return restTemplate.getForObject(url, TmdbTvSearchResponse.class);
    }

    public List<SearchHistory> getHistory() {
        return repository.findAllByOrderBySearchedAtDesc();
    }
}