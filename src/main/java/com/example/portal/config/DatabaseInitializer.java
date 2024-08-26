package com.example.portal.config;

import com.example.portal.model.Role;
import com.example.portal.model.User;
import com.example.portal.repository.RoleRepository;
import com.example.portal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Configuration
public class DatabaseInitializer {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @PostConstruct
    public void init() {
        // Create default roles if not exist
        Role userRole = roleRepository.findByName("ROLE_USER").orElseGet(() -> roleRepository.save(new Role("ROLE_USER")));
        Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseGet(() -> roleRepository.save(new Role("ROLE_ADMIN")));

        // Create default users if not exist
        createUserIfNotExist("user", new HashSet<>(Set.of(userRole)));
        createUserIfNotExist("admin", new HashSet<>(Set.of(adminRole)));
    }

    private void createUserIfNotExist(String username, Set<Role> roles) {
        if (!userRepository.existsById(username)) {
            User user = new User(username, roles);
            userRepository.save(user);
        }
    }
}
