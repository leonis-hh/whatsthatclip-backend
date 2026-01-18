package com.whatsthatclip.backend.service;

import com.whatsthatclip.backend.dto.AnalyzeRequest;
import com.whatsthatclip.backend.dto.AnalyzeResponse;
import com.whatsthatclip.backend.entity.SearchHistory;
import com.whatsthatclip.backend.gemini.*;
import com.whatsthatclip.backend.repository.SearchHistoryRepository;
import com.whatsthatclip.backend.tmdb.TmdbMovieResult;
import com.whatsthatclip.backend.tmdb.TmdbSearchResponse;
import com.whatsthatclip.backend.tmdb.TmdbTvResult;
import com.whatsthatclip.backend.tmdb.TmdbTvSearchResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class AnalyzeService {
    private SearchHistoryRepository repository;
    private RestTemplate restTemplate;

    @Value("${tmdb.api.key}")
    private String apiKey;
    @Value("${gemini.api.key}")
    private String geminiApiKey;

    public AnalyzeService(SearchHistoryRepository repository) {
        this.repository = repository;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);  // 10 seconds to connect
        factory.setReadTimeout(120000);    // 120 seconds to read (2 minutes)
        this.restTemplate = new RestTemplate(factory);
    }

    public AnalyzeResponse analyze(AnalyzeRequest request) {
        String videoUrl = request.getVideoUrl();
        if (videoUrl.startsWith("http")) {
            try {
                String result = downloadVideo(videoUrl);
                if (result == null) {
                    AnalyzeResponse response = new AnalyzeResponse();
                    response.setMessage("Failed to download video");
                    return response;
                }

                List<String> videoPaths = extractFrames(result);
                if (videoPaths == null || videoPaths.isEmpty()) {
                    AnalyzeResponse response = new AnalyzeResponse();
                    response.setMessage("Failed to extract frames. Video path: " + result);
                    return response;
                }
                GeminiResponse geminiResponse = geminiFramesAnalyzation(videoPaths);
                String geminiText = geminiResponse.getCandidates().get(0).getContent().getParts().get(0).getText();
                AnalyzeResponse response = new AnalyzeResponse();
                response.setMessage("Gemini said: " + geminiText);
                return response;
            } catch (IOException e) {
                AnalyzeResponse response = new AnalyzeResponse();
                response.setMessage("There was an error processing the video " + videoUrl);
                return response;
            }

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
            double interval = videoLength/6;
            List<String> outputPaths = new ArrayList<>();
            for (int i = 1; i < 5; i++) {
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

    private GeminiResponse geminiFramesAnalyzation (List<String> imgPaths) throws IOException{
        List<Part> parts = new ArrayList<>();
        Part part1 = new Part();
        part1.setText("These 5 frames are sequentially sampled from a single video clip. Identify the movie or TV show title and provide a confidence score.");
        parts.add(part1);
        for (String imgPath : imgPaths) {
            Part part = new Part();
            InlineData inlineData = new InlineData();
            inlineData.setMime_type("image/jpeg");
            inlineData.setData(imageToBase64(imgPath));
            part.setInline_data(inlineData);
            parts.add(part);

        }
        Content content = new Content();
        content.setParts(parts);
        List<Content> contents = new ArrayList<>();
        contents.add(content);
        Part systemPart = new Part();
        systemPart.setText("You are a movie and TV show identification microservice. Your goal is to analyze visual frames to identify the source media.\n" +
                "Operational Rules:\n" +
                "1. Return ONLY a valid JSON object. Do not include markdown formatting or conversational text.\n" +
                "2. Ignore user-interface overlays (TikTok/Reels captions, likes, or watermarks).\n" +
                "3. Analyze actors' faces, set architecture, and cinematic lighting to determine the title.\n" +
                "4. If the frames are too generic or blurry to identify with at least 80% certainty, set the title to \"UNCERTAIN\".");
        List<Part> systemParts = new ArrayList<>();
        systemParts.add(systemPart);
        SystemInstruction systemInstruction = new SystemInstruction();
        systemInstruction.setParts(systemParts);
        GenerationConfig generationConfig = new GenerationConfig();
        generationConfig.setResponse_mime_type("application/json");
        GeminiRequest geminiRequest = new GeminiRequest();
        geminiRequest.setSystem_instruction(systemInstruction);
        geminiRequest.setContents(contents);
        geminiRequest.setGeneration_config(generationConfig);
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3-pro-preview:generateContent?key=" + geminiApiKey;
        return restTemplate.postForObject(url, geminiRequest, GeminiResponse.class);
    }

    private String imageToBase64 (String imgPath) throws IOException {
        byte[] bytes = Files.readAllBytes(Path.of(imgPath));
        return Base64.getEncoder().encodeToString(bytes);
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