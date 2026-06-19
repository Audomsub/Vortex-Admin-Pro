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

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

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
