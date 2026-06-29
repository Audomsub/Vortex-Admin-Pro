package com.vortexadmin.security.oauth2;

import com.vortexadmin.entity.Role;
import com.vortexadmin.entity.User;
import com.vortexadmin.repository.RoleRepository;
import com.vortexadmin.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String picture = oauth2User.getAttribute("picture");

        if (email != null) {
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
            }
            userRepository.save(user);
        }

        return oauth2User;
    }
}
