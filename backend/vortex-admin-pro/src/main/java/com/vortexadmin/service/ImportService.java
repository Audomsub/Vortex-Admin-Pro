package com.vortexadmin.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class ImportService {

    // @Autowired private UserRepository userRepository;

    public int importUsersFromCsv(MultipartFile file) throws Exception {
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean firstRow = true;
            while ((line = reader.readLine()) != null) {
                if (firstRow) {
                    firstRow = false;
                    continue; // skip header
                }
                String[] columns = line.split(",");
                if (columns.length >= 2) {
                    // String username = columns[0].replace("\"", "");
                    // String email = columns[1].replace("\"", "");
                    // Create User entity and save
                    count++;
                }
            }
        }
        return count;
    }
}
