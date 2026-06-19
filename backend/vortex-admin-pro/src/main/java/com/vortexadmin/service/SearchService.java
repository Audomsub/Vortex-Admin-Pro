package com.vortexadmin.service;

import com.vortexadmin.repository.TaskRepository;
import com.vortexadmin.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    public Map<String, List<Map<String, Object>>> search(String query) {
        Map<String, List<Map<String, Object>>> results = new HashMap<>();

        // 1. Search Users by username or email containing the query (case-insensitive)
        List<Map<String, Object>> users = userRepository.findByDeletedAtIsNull().stream()
                .filter(u -> (u.getUsername() != null && u.getUsername().toLowerCase().contains(query.toLowerCase()))
                        || (u.getEmail() != null && u.getEmail().toLowerCase().contains(query.toLowerCase())))
                .map(u -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", u.getId());
                    item.put("title", u.getUsername());
                    item.put("description", u.getEmail());
                    item.put("url", "/users/" + u.getId());
                    return item;
                })
                .collect(Collectors.toList());
        results.put("users", users);

        // 2. Search Tasks by title or description containing the query (case-insensitive)
        List<Map<String, Object>> tasks = taskRepository.findAll().stream()
                .filter(t -> (t.getTitle() != null && t.getTitle().toLowerCase().contains(query.toLowerCase()))
                        || (t.getDescription() != null && t.getDescription().toLowerCase().contains(query.toLowerCase())))
                .map(t -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", t.getId());
                    item.put("title", t.getTitle());
                    item.put("description", t.getDescription() != null ? t.getDescription() : "");
                    item.put("status", t.getStatus());
                    item.put("url", "/tasks/" + t.getId());
                    return item;
                })
                .collect(Collectors.toList());
        results.put("tasks", tasks);

        // 3. Search Pages (static navigation entries)
        List<Map<String, Object>> pages = new ArrayList<>();
        String[] allPages = {"Dashboard", "Users", "Roles", "Settings", "Webhooks", "Billing", "Organizations"};
        for (String page : allPages) {
            if (page.toLowerCase().contains(query.toLowerCase())) {
                Map<String, Object> p = new HashMap<>();
                p.put("id", "nav-" + page.toLowerCase());
                p.put("title", page);
                p.put("url", "/" + page.toLowerCase());
                pages.add(p);
            }
        }
        results.put("pages", pages);

        return results;
    }
}
