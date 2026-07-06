package com.vortexadmin.controller;

import com.vortexadmin.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles HTTP requests for global full-text search across multiple entity types
 * (users, tasks, teams, etc.), delegating the search logic to SearchService.
 */
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    /**
     * Performs a global search across all supported entity types for the authenticated user's tenant.
     *
     * @param query the search query string to match against entity names, descriptions, and other fields
     * @return a map containing categorized search results keyed by entity type
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> globalSearch(@RequestParam("q") String query) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Search results fetched successfully");
        response.put("data", searchService.search(query));

        return ResponseEntity.ok(response);
    }
}
