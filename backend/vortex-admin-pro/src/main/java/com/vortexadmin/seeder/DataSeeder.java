package com.vortexadmin.seeder;

import com.vortexadmin.entity.Organization;
import com.vortexadmin.entity.OrganizationMember;
import com.vortexadmin.entity.Permission;
import com.vortexadmin.entity.Role;
import com.vortexadmin.entity.SubscriptionPlan;
import com.vortexadmin.entity.User;
import com.vortexadmin.entity.SystemSetting;
import com.vortexadmin.entity.Team;
import com.vortexadmin.repository.OrganizationMemberRepository;
import com.vortexadmin.repository.OrganizationRepository;
import com.vortexadmin.repository.PermissionRepository;
import com.vortexadmin.repository.RoleRepository;
import com.vortexadmin.repository.SubscriptionPlanRepository;
import com.vortexadmin.repository.SystemSettingRepository;
import com.vortexadmin.repository.TeamRepository;
import com.vortexadmin.repository.UserRepository;

import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SystemSettingRepository systemSettingRepository;
    private final TeamRepository teamRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        seedPermissionsAndRoles();
        seedSubscriptionPlans();
        seedMockData();
    }

    private void seedPermissionsAndRoles() {
        // 1. Define Permission Arrays based on hierarchy
        List<String> userPerms = Arrays.asList(
                "dashboard.view", "profile.view", "profile.update", "password.change",
                "task.read.own", "task.update.own", "calendar.read.own",
                "notes.create", "notes.read.own", "notes.update.own", "notes.delete.own",
                "file.upload.own", "file.read.own", "file.delete.own",
                "notification.read", "chat.send", "chat.read",
                "organization.create", "organization.manage", "organization.invite", "organization.delete"
        );

        List<String> managerPerms = Arrays.asList(
                "user.read", "team.read", "team.create", "team.update", "team.assign",
                "task.create", "task.read", "task.update", "task.assign",
                "calendar.create", "calendar.update", "report.view.team", "dashboard.team",
                "file.read.team", "notes.read.team"
        );

        List<String> adminPerms = Arrays.asList(
                "user.create", "user.update", "user.suspend", "role.read", "permission.read",
                "team.delete", "report.view", "report.export", "audit.read",
                "file.read.all", "file.delete.all", "notification.create", "notification.delete",
                "email.send", "email.logs.read", "api.read", "api.create", "settings.view", "billing.view"
        );

        List<String> superAdminPerms = Arrays.asList(
                "user.delete", "user.restore", "role.create", "role.update", "role.delete",
                "permission.create", "permission.update", "permission.delete",
                "settings.manage", "security.manage", "smtp.manage", "api.delete", "api.revoke",
                "billing.manage", "audit.delete", "system.backup", "system.restore", "system.maintenance",
                "database.manage", "server.manage", "super.admin"
        );

        // 2. Save all permissions to DB and collect them in Sets
        Set<Permission> userPermissionSet = saveAndGetPermissions(userPerms);
        
        Set<Permission> managerPermissionSet = saveAndGetPermissions(managerPerms);
        managerPermissionSet.addAll(userPermissionSet); // Hierarchy: Manager gets User
        
        Set<Permission> adminPermissionSet = saveAndGetPermissions(adminPerms);
        adminPermissionSet.addAll(managerPermissionSet); // Hierarchy: Admin gets Manager
        
        Set<Permission> superAdminPermissionSet = saveAndGetPermissions(superAdminPerms);
        superAdminPermissionSet.addAll(adminPermissionSet); // Hierarchy: SuperAdmin gets Admin

        // 3. Create or Update Roles with these exact sets
        Role userRole = createOrUpdateRole("USER", "Standard User", userPermissionSet);
        Role managerRole = createOrUpdateRole("MANAGER", "Team Manager", managerPermissionSet);
        Role adminRole = createOrUpdateRole("ADMIN", "Administrator", adminPermissionSet);
        Role superAdminRole = createOrUpdateRole("SUPER_ADMIN", "System Administrator with all privileges", superAdminPermissionSet);

        // 4. Create Default Super Admin if not exists
        Optional<User> adminOpt = userRepository.findByUsername("admin");
        if (adminOpt.isEmpty()) {
            User admin = User.builder()
                    .username("admin")
                    .email("admin@vortex.com")
                    .password(passwordEncoder.encode("password123"))
                    .firstName("Super")
                    .lastName("Admin")
                    .status("Active")
                    .role(superAdminRole)
                    .failedLoginAttempts(0)
                    .build();
            userRepository.save(admin);
            System.out.println("======================================================");
            System.out.println("Default Super Admin created: admin / password123");
            System.out.println("======================================================");
        } else {
            User admin = adminOpt.get();
            admin.setRole(superAdminRole);
            userRepository.save(admin);
        }
    }

    private Set<Permission> saveAndGetPermissions(List<String> codes) {
        Set<Permission> set = new HashSet<>();
        for (String code : codes) {
            Permission perm = permissionRepository.findByCode(code).orElseGet(() -> {
                Permission newPerm = Permission.builder()
                        .code(code)
                        .name(capitalize(code.replace(".", " ")))
                        .build();
                return permissionRepository.save(newPerm);
            });
            set.add(perm);
        }
        return set;
    }

    private Role createOrUpdateRole(String name, String description, Set<Permission> permissions) {
        Role role = roleRepository.findByName(name).orElseGet(() -> 
            Role.builder()
                .name(name)
                .description(description)
                .build()
        );
        role.setPermissions(permissions);
        return roleRepository.save(role);
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        String[] words = str.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }

    private void seedSubscriptionPlans() {
        seedPlan("FREE", BigDecimal.ZERO, BigDecimal.ZERO, 5, 1024L);
        seedPlan("PRO", new BigDecimal("29.00"), new BigDecimal("290.00"), 25, 10_240L);
        seedPlan("BUSINESS", new BigDecimal("99.00"), new BigDecimal("990.00"), 100, 102_400L);
        seedPlan("ENTERPRISE", new BigDecimal("299.00"), new BigDecimal("2990.00"), 0, 1_048_576L); // 0 = unlimited users
    }

    private void seedPlan(String name, BigDecimal monthly, BigDecimal yearly, int maxUsers, Long maxStorageMb) {
        if (!subscriptionPlanRepository.existsByName(name)) {
            subscriptionPlanRepository.save(SubscriptionPlan.builder()
                    .name(name)
                    .monthlyPrice(monthly)
                    .yearlyPrice(yearly)
                    .maxUsers(maxUsers)
                    .maxStorageMb(maxStorageMb)
                    .build());
        }
    }

    private void seedMockData() {
        if (userRepository.count() <= 1) { // Only admin exists
            Role adminRole = roleRepository.findByName("ADMIN").get();
            Role managerRole = roleRepository.findByName("MANAGER").get();
            Role userRole = roleRepository.findByName("USER").get();

            User john = User.builder().username("john_doe").email("john@vortex.com").password(passwordEncoder.encode("password123")).firstName("John").lastName("Doe").status("Active").role(adminRole).failedLoginAttempts(0).build();
            User jane = User.builder().username("jane_smith").email("jane@vortex.com").password(passwordEncoder.encode("password123")).firstName("Jane").lastName("Smith").status("Active").role(managerRole).failedLoginAttempts(0).build();
            User bob = User.builder().username("bob_wilson").email("bob@vortex.com").password(passwordEncoder.encode("password123")).firstName("Bob").lastName("Wilson").status("Inactive").role(userRole).failedLoginAttempts(0).build();
            userRepository.save(john);
            userRepository.save(jane);
            userRepository.save(bob);
        }

        if (systemSettingRepository.count() == 0) {
            systemSettingRepository.save(SystemSetting.builder().settingKey("site_name").settingValue("ASSA Vortex Admin").description("The name of the website").build());
            systemSettingRepository.save(SystemSetting.builder().settingKey("support_email").settingValue("support@vortex.com").description("Contact email").build());
            systemSettingRepository.save(SystemSetting.builder().settingKey("theme").settingValue("dark").description("Default theme").build());
        }

        if (teamRepository.count() == 0) {
            teamRepository.save(Team.builder().name("Engineering").description("Core Dev Team").build());
            teamRepository.save(Team.builder().name("Marketing").description("Growth and Ads").build());
            teamRepository.save(Team.builder().name("Sales").description("Outbound Sales").build());
        }

        if (organizationRepository.count() == 0) {
            userRepository.findByUsername("admin").ifPresent(admin -> {
                Organization org = organizationRepository.save(Organization.builder()
                        .name("Vortex Inc")
                        .slug("vortex-inc")
                        .planType("FREE")
                        .owner(admin)
                        .build());
                organizationMemberRepository.save(OrganizationMember.builder()
                        .organization(org)
                        .user(admin)
                        .role("OWNER")
                        .build());
            });
        }
    }
}
