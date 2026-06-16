import random
import datetime

def generate_sql():
    with open('insert_200_data.sql', 'w', encoding='utf-8') as f:
        f.write("-- Insert 200 Roles\n")
        # Generate 200 roles
        for i in range(1, 201):
            name = f"ROLE_{i}"
            desc = f"Description for role {i}"
            created_at = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
            f.write(f"INSERT INTO roles (name, description, created_at) VALUES ('{name}', '{desc}', '{created_at}');\n")
            
        f.write("\n-- Insert 200 Users\n")
        # Generate 200 users
        statuses = ['ACTIVE', 'INACTIVE', 'BANNED', 'PENDING']
        for i in range(1, 201):
            username = f"user_{i}"
            email = f"user{i}@example.com"
            password = "hashed_password_placeholder" # In a real system this would be a bcrypt hash
            first_name = f"First{i}"
            last_name = f"Last{i}"
            status = random.choice(statuses)
            created_at = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
            # Assign random role from 1 to 200
            role_id = random.randint(1, 200)
            
            f.write(f"INSERT INTO users (username, email, password, first_name, last_name, status, created_at, role_id, failed_login_attempts) ")
            f.write(f"VALUES ('{username}', '{email}', '{password}', '{first_name}', '{last_name}', '{status}', '{created_at}', {role_id}, 0);\n")
            
        f.write("\n-- Insert 200 Tasks\n")
        # Generate 200 tasks
        task_statuses = ['TODO', 'IN_PROGRESS', 'REVIEW', 'DONE']
        priorities = ['LOW', 'MEDIUM', 'HIGH']
        for i in range(1, 201):
            title = f"Task title {i}"
            description = f"Description for task {i}"
            status = random.choice(task_statuses)
            priority = random.choice(priorities)
            created_at = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
            # Assign random user from 1 to 200
            assigned_to = random.randint(1, 200)
            
            f.write(f"INSERT INTO tasks (title, description, status, priority, created_at, assigned_to) ")
            f.write(f"VALUES ('{title}', '{description}', '{status}', '{priority}', '{created_at}', {assigned_to});\n")

if __name__ == '__main__':
    generate_sql()
    print("Successfully generated insert_200_data.sql")
