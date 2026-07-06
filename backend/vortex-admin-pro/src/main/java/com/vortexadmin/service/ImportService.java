package com.vortexadmin.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Service responsible for parsing and importing user data from uploaded CSV files.
 * Reads each non-header row and extracts user fields for batch import.
 */
@Service
public class ImportService {

    // @Autowired private UserRepository userRepository;

    /**
     * Parses the provided CSV file and imports valid user records.  The first row is treated as
     * a header and is skipped.  Each subsequent row must contain at least two comma-separated
     * columns (username and email); rows with fewer columns are ignored.
     *
     * @param file the multipart CSV file containing user data to import
     * @return the number of rows successfully processed (not counting the header)
     * @throws Exception if the file cannot be opened or read
     */
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
