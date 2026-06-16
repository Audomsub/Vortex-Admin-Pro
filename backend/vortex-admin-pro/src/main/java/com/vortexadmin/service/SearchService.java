package com.vortexadmin.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SearchService {

    // Inject repositories
    // @Autowired private UserRepository userRepository;
    // @Autowired private TaskRepository taskRepository;

    public Map<String, List<Map<String, Object>>> search(String query) {
        Map<String, List<Map<String, Object>>> results = new HashMap<>();
        
        // 1. Search Users
        List<Map<String, Object>> users = new ArrayList<>();
        // e.g. userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query)
        // Dummy data for now
        if ("admin".contains(query.toLowerCase())) {
            Map<String, Object> user = new HashMap<>();
            user.put("id", 1);
            user.put("title", "admin");
            user.put("description", "Super Admin");
            users.add(user);
        }
        results.put("users", users);

        // 2. Search Tasks
        List<Map<String, Object>> tasks = new ArrayList<>();
        // e.g. taskRepository.findByTitleContainingIgnoreCase(query)
        if ("setup server".contains(query.toLowerCase())) {
            Map<String, Object> task = new HashMap<>();
            task.put("id", 101);
            task.put("title", "Setup server");
            tasks.add(task);
        }
        results.put("tasks", tasks);

        // 3. Search Pages (Static or Database)
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
