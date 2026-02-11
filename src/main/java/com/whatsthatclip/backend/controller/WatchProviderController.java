package com.whatsthatclip.backend.controller;

import com.whatsthatclip.backend.service.TmdbService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class WatchProviderController {
    private TmdbService tmdbService;
    public WatchProviderController(TmdbService tmdbService) {
        this.tmdbService = tmdbService;
    }

    @GetMapping("/api/watch/{type}/{id}")
    public Map<String, Object> getWatchProviders(@PathVariable String type, @PathVariable Integer id, @RequestParam(defaultValue = "US") String country) {
        return tmdbService.getWatchProviders(id,type,country);
    }

}
