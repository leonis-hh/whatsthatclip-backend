package com.whatsthatclip.backend.service;

import com.whatsthatclip.backend.entity.SearchHistory;
import com.whatsthatclip.backend.entity.User;
import com.whatsthatclip.backend.repository.SearchHistoryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SearchHistoryService {
    private SearchHistoryRepository searchRepository;
    private UserService userService;

    public SearchHistoryService (SearchHistoryRepository searchRepository, UserService userService) {
        this.searchRepository = searchRepository;
        this.userService = userService;
    }

    public SearchHistory saveSearch(String videoUrl, String title, String type, String year, String overview, String posterUrl, User user) {
        SearchHistory search = new SearchHistory(videoUrl, title, type, year, overview, posterUrl, LocalDateTime.now(), user);
        return searchRepository.save(search);
    }

    public List<SearchHistory> getHistoryForUser () {
        User user = userService.getCurrentUser();
        return searchRepository.findByUserOrderBySearchedAtDesc(user);
    }


}
