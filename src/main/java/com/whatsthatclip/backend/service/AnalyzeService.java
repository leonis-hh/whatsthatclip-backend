package com.whatsthatclip.backend.service;

import com.google.genai.Client;
import com.google.genai.types.*;
import com.whatsthatclip.backend.dto.AnalyzeRequest;
import com.whatsthatclip.backend.dto.AnalyzeResponse;
import com.whatsthatclip.backend.entity.SearchHistory;
import com.whatsthatclip.backend.entity.User;
import com.whatsthatclip.backend.repository.SearchHistoryRepository;
import com.whatsthatclip.backend.tmdb.TmdbMovieResult;
import com.whatsthatclip.backend.tmdb.TmdbSearchResponse;
import com.whatsthatclip.backend.tmdb.TmdbTvResult;
import com.whatsthatclip.backend.tmdb.TmdbTvSearchResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class AnalyzeService {
    private UserService userService;
    private SearchHistoryService searchService;
    private RestTemplate restTemplate = new RestTemplate();

    @Value("${tmdb.api.key}")
    private String apiKey;
    @Value("${gemini.api.key}")
    private String geminiApiKey;

    public AnalyzeService(SearchHistoryService searchService, UserService userService) {
        this.searchService = searchService;
        this.userService = userService;
    }

    public AnalyzeResponse analyze(AnalyzeRequest request) {
        String videoUrl = request.getVideoUrl();
            try {
                String result = downloadVideo(videoUrl);
                if (result == null) {
                    return errorResponse("Failed to download video");
                }

                List<String> videoPaths = extractFrames(result);
                if (videoPaths == null || videoPaths.isEmpty()) {
                    return errorResponse("Failed to extract frames. Video path: " + result);
                }
                String geminiText = geminiFramesAnalyzation(videoPaths);
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> geminiResult;
                if (geminiText.trim().startsWith("[")) {
                    List<Map> geminiList = mapper.readValue(geminiText, List.class);
                    geminiResult = geminiList.get(0);
                } else {
                    geminiResult = mapper.readValue(geminiText, Map.class);

                }
                String title = (String) geminiResult.get("title");
                if ("UNCERTAIN".equals(title)) {
                    return errorResponse("We were unable to identify, we will be working on providing a solution soon");
                }

                System.out.println("Gemini identified: " + title);
                TmdbSearchResponse movieResults = searchMovie(title);
                TmdbTvSearchResponse tvResults = searchTv(title);

                TmdbMovieResult movie = getTopMovie(movieResults);
                TmdbTvResult tv = getTopTv(tvResults);

                if (movie == null && tv == null) {
                    return errorResponse("No results found");
                }

                String finalTitle, type, date, overview, posterPath;

                if (movie != null && (tv == null || movie.getPopularity() > tv.getPopularity())) {
                    finalTitle = movie.getTitle();
                    type = "Movie";
                    date = movie.getRelease_date();
                    overview = movie.getOverview();
                    posterPath = movie.getPoster_path();
                } else {
                    finalTitle = tv.getName();
                    type = "TV Show";
                    date = tv.getFirst_air_date();
                    overview = tv.getOverview();
                    posterPath = tv.getPoster_path();
                }

                return buildFinalResponse(finalTitle, type, date, overview, posterPath, videoUrl);


            } catch (IOException e) {
                return errorResponse("There was an error processing the video " + videoUrl);
            }


    }

    private AnalyzeResponse errorResponse(String message) {
        AnalyzeResponse response = new AnalyzeResponse();
        response.setMessage(message);
        return response;
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

    private String geminiFramesAnalyzation(List<String> imgPaths) throws IOException {
        // Log what we're sending
        System.out.println("=== GEMINI REQUEST (CANONICAL TITLE MODE) ===");
        System.out.println("Number of frames: " + imgPaths.size());
        for (int i = 0; i < imgPaths.size(); i++) {
            File f = new File(imgPaths.get(i));
            System.out.println("Frame " + (i + 1) + ": " + f.getName() + " (" + f.length() / 1024 + " KB)");
        }

        Client client = Client.builder()
                .apiKey(geminiApiKey)
                .httpOptions(HttpOptions.builder()
                        .timeout(300000)
                        .build())
                .build();

        String currentDate = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("MMMM yyyy"));

        List<Part> parts = new ArrayList<>();

        // Critical user prompt for canonical title only
        parts.add(Part.fromText(
                "You are a movie/TV identification API.\n\n" +
                        "TASK:\n" +
                        "1. Identify the movie or TV show ONLY if it can be resolved to an OFFICIAL, RELEASED title.\n" +
                        "2. Working titles or internal project names are FORBIDDEN.\n" +
                        "3. Use Google Search ONLY to confirm the official title and release year, not to guess.\n" +
                        "4. NO reasoning, NO evidence, NO speculation.\n" +
                        "5. If the official title cannot be confidently resolved, return UNCERTAIN.\n\n" +
                        "OUTPUT JSON ONLY:\n" +
                        "{ \"title\": \"Exact Official Title or UNCERTAIN\", \"year\": number or null, \"confidence\": number (0-100) }"
        ));

        // Add image frames
        for (String imgPath : imgPaths) {
            byte[] imageBytes = Files.readAllBytes(Path.of(imgPath));
            parts.add(Part.fromBytes(imageBytes, "image/jpeg"));
        }

        Content content = Content.fromParts(parts.toArray(new Part[0]));

        GenerateContentConfig config = GenerateContentConfig.builder()
                .responseMimeType("application/json")
                .systemInstruction(Content.builder()
                        .parts(List.of(Part.fromText(
                                "System role: authoritative media identifier.\n" +
                                        "Current date: " + currentDate + "\n\n" +
                                        "CRITICAL RULES:\n" +
                                        "1. Only return officially released titles.\n" +
                                        "2. Never return working titles or inferred names.\n" +
                                        "3. If multiple titles exist, choose the distributor-released one.\n" +
                                        "4. Confidence above 85 requires strong certainty.\n" +
                                        "5. If any doubt exists → return UNCERTAIN."
                        ))).build())
                .tools(List.of(
                        Tool.builder()
                                .googleSearch(GoogleSearch.builder().build())
                                .build()
                ))
                .build();

        GenerateContentResponse response = client.models.generateContent(
                "gemini-3-pro-preview",
                content,
                config
        );

        // Log Gemini response
        System.out.println("=== GEMINI RESPONSE ===");
        System.out.println(response.text());
        System.out.println("Model used: " + response.modelVersion());

        return response.text();
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
        User currentUser = userService.getCurrentUser();
        if (currentUser != null) {
            searchService.saveSearch(videoUrl, title, type, year, overview, posterUrl, currentUser);
        }

        return response;
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


}