package com.whatsthatclip.backend.repository;

import com.whatsthatclip.backend.entity.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {
}
