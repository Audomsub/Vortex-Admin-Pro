package com.vortexadmin.security.oauth2;

import com.vortexadmin.entity.Role;
import com.vortexadmin.entity.User;
import com.vortexadmin.repository.RoleRepository;
import com.vortexadmin.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.UUID;

/**
 * Custom Spring Security OAuth2 user service that handles the post-authorization
 * step of an OAuth2 login flow.
 *
 * <p>Extends {@link DefaultOAuth2UserService} to intercept the standard OAuth2 user-info
 * response and implement a "find or create" strategy: existing users (matched by email)
 * are returned as-is (with optional avatar updates), while first-time OAuth2 users are
 * automatically provisioned in the database with the default {@code USER} role.
 *
 * <p>This service is wired into the security configuration via:
 * <pre>
 *   .oauth2Login(oauth2 -&gt; oauth2
 *       .userInfoEndpoint(userInfo -&gt; userInfo.userService(customOAuth2UserService)))
 * </pre>
 *
 * <p><strong>Supported providers:</strong> Any OAuth2 provider that supplies an
 * {@code email}, {@code name}, and {@code picture} attribute in its user-info response
 * (e.g., Google). Providers that do not supply an email cause an
 * {@link OAuth2AuthenticationException} with error code {@code "missing_email"}.
 *
 * <p>The method is annotated with {@code @Transactional} to ensure that database
 * reads and writes (user lookup, role lookup/creation, user save) occur within a
 * single transaction, preventing partial saves on failure.
 */
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    /**
     * Loads the OAuth2 user from the provider's user-info endpoint and synchronises
     * the result with the local user database.
     *
     * <p>Processing steps:
     * <ol>
     *   <li>Delegates to {@link DefaultOAuth2UserService#loadUser(OAuth2UserRequest)} to
     *       exchange the access token for user attributes.</li>
     *   <li>Extracts {@code email}, {@code name}, and {@code picture} attributes from the
     *       OAuth2 response.</li>
     *   <li>Throws {@link OAuth2AuthenticationException} if {@code email} is absent.</li>
     *   <li>Looks up the user by email:
     *       <ul>
     *         <li>If found, checks whether the avatar URL has changed and persists any
     *             update.</li>
     *         <li>If not found, creates a new {@link User} record with the {@code USER}
     *             role (creating the role if it does not yet exist), a blank password
     *             (OAuth2 users do not authenticate with a password), and a randomly
     *             suffixed username derived from the email local-part.</li>
     *       </ul>
     *   </li>
     *   <li>Returns the original {@link OAuth2User} from the provider (not a custom
     *       wrapper), preserving full compatibility with Spring Security's OAuth2 flow.</li>
     * </ol>
     *
     * @param userRequest the OAuth2 user request containing the access token and
     *                    client registration details.
     * @return the provider's {@link OAuth2User} with no modifications.
     * @throws OAuth2AuthenticationException if the provider does not supply an email
     *                                       address ({@code error_code = "missing_email"}).
     */
    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String picture = oauth2User.getAttribute("picture");

        if (email == null) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("missing_email", "OAuth2 provider did not supply an email address", null));
        }

        User user = userRepository.findByEmail(email).orElseGet(() -> {
                Role userRole = roleRepository.findByName("USER").orElseGet(() -> {
                    Role role = Role.builder()
                            .name("USER")
                            .description("Standard User")
                            .permissions(new HashSet<>())
                            .build();
                    return roleRepository.save(role);
                });

                String firstName = "";
                String lastName = "";
                if (name != null) {
                    String[] parts = name.split(" ", 2);
                    firstName = parts[0];
                    lastName = parts.length > 1 ? parts[1] : "";
                }

                String username = email.split("@")[0] + "_" + UUID.randomUUID().toString().substring(0, 4);
                return User.builder()
                        .username(username)
                        .email(email)
                        .firstName(firstName)
                        .lastName(lastName)
                        .avatarUrl(picture)
                        .password("")
                        .role(userRole)
                        .status("Active")
                        .failedLoginAttempts(0)
                        .build();
            });

        if (picture != null && !picture.equals(user.getAvatarUrl())) {
            user.setAvatarUrl(picture);
            userRepository.save(user);
        } else if (user.getId() == null) {
            userRepository.save(user);
        }

        return oauth2User;
    }
}
