package com.whatsthatclip.backend.service;

import com.whatsthatclip.backend.dto.AnalyzeRequest;
import com.whatsthatclip.backend.dto.AnalyzeResponse;
import com.whatsthatclip.backend.entity.SearchHistory;
import com.whatsthatclip.backend.repository.SearchHistoryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AnalyzeService {
    private SearchHistoryRepository repository;

    public AnalyzeService(SearchHistoryRepository repository) {
        this.repository = repository;
    }

    public SearchHistory saveSearch(String videoUrl, String title, String type, String year, String overview, String posterUrl) {
        SearchHistory search = new SearchHistory(videoUrl,title,type,year,overview,posterUrl, LocalDateTime.now());
        return repository.save(search);
    }

    public AnalyzeResponse analyze(AnalyzeRequest request) {
        String videoUrl = request.getVideoUrl();
        String title = "Title";
        String type = "Movie/TV Show";
        String year = "2026";
        String overview = "Overview";
        String posterUrl="http://example.com/poster.jpg";
        AnalyzeResponse response = new AnalyzeResponse();
        response.setTitle(title);
        response.setType(type);
        response.setYear(year);
        response.setOverview(overview);
        response.setPosterUrl(posterUrl);
        response.setMessage("Recieved URL: " + videoUrl );
        saveSearch(videoUrl,title,type,year,overview,posterUrl);
        return response;
    }

    public List<SearchHistory> getHistory () {
        return repository.findAllByOrderBySearchedAtDesc();
    }
}
