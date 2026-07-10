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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Application startup component that seeds the Vortex Admin Pro database with the
 * minimum reference data required for a functional system.
 *
 * <p>Implements {@link CommandLineRunner} so that Spring Boot automatically invokes
 * {@link #run(String...)} after the application context is fully initialized, before
 * serving any HTTP requests. The entire run is wrapped in a single database transaction
 * via {@code @Transactional}.
 *
 * <p>The seeder is <strong>idempotent</strong>: every operation checks for the existence
 * of the target record before inserting it, making it safe to run on every application
 * restart without creating duplicates.
 *
 * <p>Three top-level seeding groups are performed in order:
 * <ol>
 *   <li>{@link #seedPermissionsAndRoles()} – creates the four RBAC roles
 *       ({@code USER}, {@code MANAGER}, {@code ADMIN}, {@code SUPER_ADMIN}) with their
 *       cumulative permission sets, and provisions the default super-admin account.</li>
 *   <li>{@link #seedSubscriptionPlans()} – creates the {@code FREE}, {@code PRO},
 *       {@code BUSINESS}, and {@code ENTERPRISE} billing tiers.</li>
 *   <li>{@link #seedMockData()} – inserts sample users, system settings, teams, and a
 *       default organisation when the database contains only the seeded admin account.</li>
 * </ol>
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SystemSettingRepository systemSettingRepository;
    private final TeamRepository teamRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;

    /**
     * Entry point invoked by Spring Boot after the application context is ready.
     *
     * <p>Delegates sequentially to the three seeding methods. The method signature
     * accepts varargs {@code args} passed from the command line but they are not
     * used by this implementation.
     *
     * @param args command-line arguments passed to the Spring Boot application
     *             (unused by this seeder).
     * @throws Exception if any database operation fails and causes the transaction
     *                   to roll back.
     */
    @Override
    @Transactional
    public void run(String... args) throws Exception {
        seedPermissionsAndRoles();
        seedSubscriptionPlans();
        seedMockData();
    }

    /**
     * Seeds all RBAC permissions and roles using a hierarchical permission model.
     *
     * <p>Permissions are grouped into four tiers that mirror the four application roles.
     * Each higher-tier role inherits all permissions of every lower tier:
     * <ul>
     *   <li>{@code USER} – personal profile, own tasks, own files, and organisation management.</li>
     *   <li>{@code MANAGER} – inherits USER plus team management, task assignment, and team reports.</li>
     *   <li>{@code ADMIN} – inherits MANAGER plus user administration, audit access, and billing view.</li>
     *   <li>{@code SUPER_ADMIN} – inherits ADMIN plus destructive operations (delete, security settings,
     *       database management, system maintenance).</li>
     * </ul>
     *
     * <p>After roles are created, the default super-admin account ({@code admin} /
     * {@code password123}) is provisioned if it does not already exist. If the account exists
     * but has a {@code null} role (e.g., due to a previous partial migration), the
     * {@code SUPER_ADMIN} role is reattached without overriding any intentional role change.
     */
    private void seedPermissionsAndRoles() {
        // 1. Define Permission Arrays based on hierarchy
        List<String> userPerms = Arrays.asList(
                "dashboard.view", "profile.view", "profile.update", "password.change",
                "task.read.own", "task.update.own", "task.delete.own", "calendar.read.own",
                "notes.create", "notes.read.own", "notes.update.own", "notes.delete.own",
                "file.upload.own", "file.read.own", "file.delete.own",
                "notification.read", "chat.send", "chat.read",
                "organization.create", "organization.manage", "organization.invite", "organization.delete"
        );

        List<String> managerPerms = Arrays.asList(
                "user.read", "team.read", "team.create", "team.update", "team.assign",
                "task.create", "task.read", "task.update", "task.delete", "task.assign",
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
            logger.info("Default Super Admin created: username=admin");
        } else {
            // Only repair a missing role — never override an intentional role change
            User admin = adminOpt.get();
            if (admin.getRole() == null) {
                admin.setRole(superAdminRole);
                userRepository.save(admin);
            }
        }

    }

    /**
     * Ensures that all permission codes in the provided list exist in the database,
     * creating any that are missing, and returns the full set of managed
     * {@link Permission} entities.
     *
     * <p>For each code, an {@code upsert} pattern is used: the repository is queried
     * first, and a new {@link Permission} is persisted only if no record with that code
     * exists. This keeps the method idempotent across restarts.
     *
     * <p>The permission name is derived from the code by replacing dots with spaces and
     * capitalising the first letter of each word (e.g., {@code "user.read"} becomes
     * {@code "User Read"}).
     *
     * @param codes the list of permission code strings to ensure exist in the database.
     * @return a mutable {@link Set} containing the managed {@link Permission} entities
     *         for all codes in the input list.
     */
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

    /**
     * Creates a new {@link Role} or updates the permissions of an existing role with the
     * given name.
     *
     * <p>Uses an upsert pattern: if a role with the given {@code name} is found in the
     * database its permission set is replaced with {@code permissions} and the updated
     * record is saved. If no role exists, a new one is built with the provided
     * {@code description} and {@code permissions}.
     *
     * @param name        the unique role name (e.g., {@code "ADMIN"}).
     * @param description a human-readable description stored for display purposes.
     * @param permissions the complete set of {@link Permission} entities to assign to
     *                    this role.
     * @return the saved (and managed) {@link Role} entity.
     */
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

    /**
     * Capitalises the first letter of each space-delimited word in the input string.
     *
     * <p>Used to convert a dot-notation permission code (after dot-to-space replacement)
     * into a readable display name. For example, {@code "user read"} becomes
     * {@code "User Read"}.
     *
     * @param str the input string; may be {@code null} or empty, in which case it is
     *            returned unchanged.
     * @return a new string with the first character of each word in upper case.
     */
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

    /**
     * Seeds the four standard subscription plans into the database.
     *
     * <p>Plans are created only if they do not already exist (checked by name). The
     * four tiers are:
     * <ul>
     *   <li>{@code FREE} – $0/month, 5 users, 1 GB storage.</li>
     *   <li>{@code PRO} – $29/month, 25 users, 10 GB storage.</li>
     *   <li>{@code BUSINESS} – $99/month, 100 users, 100 GB storage.</li>
     *   <li>{@code ENTERPRISE} – $299/month, unlimited users (0 = no limit), 1 TB storage.</li>
     * </ul>
     */
    private void seedSubscriptionPlans() {
        seedPlan("FREE", BigDecimal.ZERO, BigDecimal.ZERO, 5, 1024L);
        seedPlan("PRO", new BigDecimal("29.00"), new BigDecimal("290.00"), 25, 10_240L);
        seedPlan("BUSINESS", new BigDecimal("99.00"), new BigDecimal("990.00"), 100, 102_400L);
        seedPlan("ENTERPRISE", new BigDecimal("299.00"), new BigDecimal("2990.00"), 0, 1_048_576L); // 0 = unlimited users
    }

    /**
     * Persists a single {@link SubscriptionPlan} if no plan with the given name already
     * exists in the database.
     *
     * @param name          the unique plan name (e.g., {@code "PRO"}).
     * @param monthly       the monthly price as a {@link BigDecimal}.
     * @param yearly        the yearly price as a {@link BigDecimal}.
     * @param maxUsers      the maximum number of users allowed on this plan; {@code 0}
     *                      denotes unlimited.
     * @param maxStorageMb  the maximum storage quota in megabytes.
     */
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

    /**
     * Seeds sample/mock data for development and demonstration purposes.
     *
     * <p>This method only inserts data when the corresponding table is empty or contains
     * only the seeded admin account, preventing mock data from polluting a production
     * database that already has real records. The following data is seeded:
     * <ul>
     *   <li><strong>Mock users</strong> – three sample accounts ({@code john_doe} as ADMIN,
     *       {@code jane_smith} as MANAGER, {@code bob_wilson} as USER) are created if the
     *       user table has at most 1 row (the super-admin).</li>
     *   <li><strong>System settings</strong> – default key/value settings (site name,
     *       support email, theme, maintenance mode, and password policy flags) are
     *       inserted if the {@code system_settings} table is empty.</li>
     *   <li><strong>Teams</strong> – three sample teams (Engineering, Marketing, Sales)
     *       are created if the {@code teams} table is empty.</li>
     *   <li><strong>Organisation</strong> – a default {@code "Vortex Inc"} organisation
     *       owned by the admin user, with the admin as {@code OWNER} member, is created
     *       if the {@code organizations} table is empty.</li>
     * </ul>
     */
    private void seedMockData() {
        if (userRepository.count() <= 1) { // Only admin exists
            Role adminRole = roleRepository.findByName("ADMIN").orElseThrow(() -> new RuntimeException("Role ADMIN not found — seeding incomplete"));
            Role managerRole = roleRepository.findByName("MANAGER").orElseThrow(() -> new RuntimeException("Role MANAGER not found — seeding incomplete"));
            Role userRole = roleRepository.findByName("USER").orElseThrow(() -> new RuntimeException("Role USER not found — seeding incomplete"));

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
            systemSettingRepository.save(SystemSetting.builder().settingKey("maintenance_mode").settingValue("false").description("Enable maintenance mode").build());
            systemSettingRepository.save(SystemSetting.builder().settingKey("pw_min_length").settingValue("8").description("Minimum password length").build());
            systemSettingRepository.save(SystemSetting.builder().settingKey("pw_require_uppercase").settingValue("false").description("Require uppercase letter in password").build());
            systemSettingRepository.save(SystemSetting.builder().settingKey("pw_require_lowercase").settingValue("false").description("Require lowercase letter in password").build());
            systemSettingRepository.save(SystemSetting.builder().settingKey("pw_require_digit").settingValue("false").description("Require digit in password").build());
            systemSettingRepository.save(SystemSetting.builder().settingKey("pw_require_special").settingValue("false").description("Require special character in password").build());
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
