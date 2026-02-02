package com.whatsthatclip.backend.controller;

import com.whatsthatclip.backend.entity.SearchHistory;
import com.whatsthatclip.backend.entity.User;
import com.whatsthatclip.backend.service.SearchHistoryService;
import com.whatsthatclip.backend.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SearchHistoryController {
    private SearchHistoryService searchService;

    public SearchHistoryController (SearchHistoryService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/api/history")
    public List<SearchHistory> getHistory () {
        return searchService.getHistoryForUser();
    }
}
