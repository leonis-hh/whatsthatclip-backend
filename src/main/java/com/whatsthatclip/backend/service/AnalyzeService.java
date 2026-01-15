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
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AnalyzeService {
    private SearchHistoryRepository repository;
    private RestTemplate restTemplate = new RestTemplate();

    @Value("${tmdb.api.key}")
    private String apiKey;
    @Value("${gemini.api.key}")
    private String geminiApiKey;

    public AnalyzeService(SearchHistoryRepository repository) {
        this.repository = repository;
    }

    public AnalyzeResponse analyze(AnalyzeRequest request) {
        String videoUrl = request.getVideoUrl();
        if (videoUrl.startsWith("http")) {
            AnalyzeResponse response = new AnalyzeResponse();
            response.setMessage("Video processing coming soon: " + videoUrl);
            return response;
        }

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

    private String geminiFramesAnalyzation (List<String> framePaths) {

    }

    private String downloadVideo(String url) throws IOException {
        try {
            String tempDir = System.getProperty("java.io.tmpdir");
            String fileName = "video_" + System.currentTimeMillis() + ".mp4";
            String outputPath = tempDir + "/" + fileName;
            ProcessBuilder pb = new ProcessBuilder("yt-dlp", "-o", outputPath, url);
            Process p = pb.start();
            p.waitFor();

            return outputPath;

        } catch (InterruptedException e) {
            return null;

        }

    }

    private List<String> extractFrames (String videoPath) {
        try {
            String tempDir = System.getProperty("java.io.tmpdir");
            String newDir = tempDir + "/frames_" + System.currentTimeMillis();
            File folder = new File(newDir);
            folder.mkdir();
            double videoLength = getVideoLength(videoPath);
            double interval = videoLength/4;
            List<String> outputPaths = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                double timestamp = i * interval;
                String outputPath = newDir + "/frame_" + i + ".jpg";
                ProcessBuilder pb = new ProcessBuilder(
                        "ffmpeg", "-ss", String.valueOf(timestamp), "-i", videoPath,
                        "-frames:v", "1", outputPath
                );
                Process p = pb.start();
                p.waitFor();
                outputPaths.add(outputPath);
            }
            return outputPaths;

        } catch (Exception e) {
            return null;
        }
    }

    private double getVideoLength(String videoPath) throws IOException {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "ffprobe", "-v", "error", "-show_entries", "format=duration",
                    "-of", "default=noprint_wrappers=1:nokey=1", videoPath
            );
            Process p = pb.start();
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String lengthStr = reader.readLine();
            double length= Double.parseDouble(lengthStr);
            return length;
        } catch (InterruptedException e) {
            return -1;
        }
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