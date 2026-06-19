import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class GenerateMockSql {
    public static void main(String[] args) {
        String fileName = "insert_200_data.sql";
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            Random random = new Random();

            writer.println("-- Insert 200 Roles");
            for (int i = 1; i <= 200; i++) {
                String name = "ROLE_" + i;
                String desc = "Description for role " + i;
                String createdAt = dtf.format(LocalDateTime.now());
                writer.printf("INSERT INTO roles (name, description, created_at) VALUES ('%s', '%s', '%s');%n", name, desc, createdAt);
            }

            writer.println("\n-- Insert 200 Users");
            String[] statuses = {"ACTIVE", "INACTIVE", "BANNED", "PENDING"};
            for (int i = 1; i <= 200; i++) {
                String username = "user_" + i;
                String email = "user" + i + "@example.com";
                String password = "hashed_password_placeholder"; // In a real system this would be a bcrypt hash
                String firstName = "First" + i;
                String lastName = "Last" + i;
                String status = statuses[random.nextInt(statuses.length)];
                String createdAt = dtf.format(LocalDateTime.now());
                int roleId = random.nextInt(200) + 1;
                
                writer.print("INSERT INTO users (username, email, password, first_name, last_name, status, created_at, role_id, failed_login_attempts) ");
                writer.printf("VALUES ('%s', '%s', '%s', '%s', '%s', '%s', '%s', %d, 0);%n", username, email, password, firstName, lastName, status, createdAt, roleId);
            }

            writer.println("\n-- Insert 200 Tasks");
            String[] taskStatuses = {"TODO", "IN_PROGRESS", "REVIEW", "DONE"};
            String[] priorities = {"LOW", "MEDIUM", "HIGH"};
            for (int i = 1; i <= 200; i++) {
                String title = "Task title " + i;
                String description = "Description for task " + i;
                String status = taskStatuses[random.nextInt(taskStatuses.length)];
                String priority = priorities[random.nextInt(priorities.length)];
                String createdAt = dtf.format(LocalDateTime.now());
                int assignedTo = random.nextInt(200) + 1;
                
                writer.print("INSERT INTO tasks (title, description, status, priority, created_at, assigned_to) ");
                writer.printf("VALUES ('%s', '%s', '%s', '%s', '%s', %d);%n", title, description, status, priority, createdAt, assignedTo);
            }

            System.out.println("Successfully generated " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
