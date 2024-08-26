package com.example.portal.security;

import com.example.portal.model.Role;
import com.example.portal.model.User;
import com.example.portal.repository.RoleRepository;
import com.example.portal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Component
public class AuthenticationSuccessListener implements ApplicationListener<AuthenticationSuccessEvent> {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();

        // Check if user already exists in the H2 database
        Optional<User> existingUser = userRepository.findById(username);
        if (!existingUser.isPresent()) {
            // Assign "USER" role to new users
            Role userRole = roleRepository.findByName("USER").orElseThrow(() ->
                    new RuntimeException("USER role not found"));

            User newUser = new User();
            newUser.setUsername(username);
            newUser.setRoles(new HashSet<>(Set.of(userRole)));

            userRepository.save(newUser);
        }
    }
}
