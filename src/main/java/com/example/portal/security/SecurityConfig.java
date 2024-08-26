package com.example.portal.security;

import com.example.portal.model.Role;
import com.example.portal.repository.RoleRepository;
import com.example.portal.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapUserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Value("${spring.ldap.urls}")
    private String ldapUrl;

    @Value("${spring.ldap.base}")
    private String ldapBase;

    @Value("${spring.ldap.username}")
    private String ldapUsername;

    @Value("${spring.ldap.password}")
    private String ldapPassword;

    @Value("${app.security.users.username}")
    private String user1Username;
    @Value("${app.security.users.password}")
    private String user1Password;
    @Value("${app.security.users.roles}")
    private String user1Roles;

    @Value("${app.security.admins.username}")
    private String admin1Username;
    @Value("${app.security.admins.password}")
    private String admin1Password;
    @Value("${app.security.admins.roles}")
    private String admin1Roles;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/error","/login", "/css/**", "/js/**", "/images/**").permitAll()  // Allow access to the login page
                        .requestMatchers("/logout").permitAll() // Explicitly allow access to /logout
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/h2-console/**").hasRole("ADMIN")
                        .requestMatchers("/user/**").hasRole("USER")
                        .anyRequest().authenticated()
                )
                .formLogin(login -> login
                        .loginPage("/login")                   // Specify the login page
                        .defaultSuccessUrl("/", true) // Redirect to home page after login
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2-console/**") // Disable CSRF for H2 console
                )
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin) // Allow frames from the same origin for H2 console - Use the new method for frame options
                )
                .sessionManagement(session -> session
                        .invalidSessionUrl("/login?sessionExpired=true")
                        .maximumSessions(1) // Allows only one session per user
                        .expiredUrl("/login?sessionExpired=true")
                );

        return http.build();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public LdapContextSource contextSource() {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(ldapUrl);
        contextSource.setBase(ldapBase);
        contextSource.setUserDn(ldapUsername);
        contextSource.setPassword(ldapPassword);
        contextSource.afterPropertiesSet();
        return contextSource;
    }

    @Bean
    public LdapAuthoritiesPopulator authoritiesPopulator(LdapContextSource contextSource) {
        DefaultLdapAuthoritiesPopulator authoritiesPopulator =
                new DefaultLdapAuthoritiesPopulator(contextSource, "");
        authoritiesPopulator.setGroupSearchFilter("(member={0})");
        return authoritiesPopulator;
    }

    @Bean
    public UserDetailsService combinedUserDetailsService(LdapContextSource contextSource, PasswordEncoder passwordEncoder) {
        // Create the LDAP-based UserDetailsService
        FilterBasedLdapUserSearch ldapUserSearch = new FilterBasedLdapUserSearch(
                "ou=Users",
                "(uid={0})",
                contextSource);

        LdapUserDetailsService ldapUserDetailsService = new LdapUserDetailsService(ldapUserSearch, authoritiesPopulator(contextSource));

        // Create the in-memory UserDetailsService
        UserDetails admin = User.builder()
                .username(admin1Username)
                .password(passwordEncoder.encode(admin1Password))
                .roles(admin1Roles)
                .build();

        UserDetails user = User.builder()
                .username(user1Username)
                .password(passwordEncoder.encode(user1Password))
                .roles(user1Roles)
                .build();

        InMemoryUserDetailsManager inMemoryUserDetailsService = new InMemoryUserDetailsManager(admin, user);

        // Combine them into a single UserDetailsService
        return username -> {
            try {
                // First, try to load the user from the in-memory user details
                return inMemoryUserDetailsService.loadUserByUsername(username);
            } catch (Exception ex) {
                logger.debug("Not found in-memory, load from LDAP and then assign roles from H2 database");
                // If not found in-memory, load from LDAP and then assign roles from H2 database
                UserDetails ldapUserDetails = ldapUserDetailsService.loadUserByUsername(username);
                Role userRole = roleRepository.findByName("ROLE_USER").orElseGet(() -> roleRepository.save(new Role("ROLE_USER")));
                Optional<com.example.portal.model.User> h2User = createUserIfNotExist(username, new HashSet<>(Set.of(userRole)));

                // Map H2 roles to Spring Security roles
                Set<GrantedAuthority> grantedAuthorities = h2User.get().getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getName()))
                        .collect(Collectors.toSet());

                // Return the LDAP user details with roles fetched from H2
                return new org.springframework.security.core.userdetails.User(
                        ldapUserDetails.getUsername(),
                        //ldapUserDetails.getPassword(), // Password from LDAP, no encoding required
                        passwordEncoder.encode(ldapUserDetails.getPassword()), // Directly use the LDAP password (no BCrypt validation)
                        grantedAuthorities // Roles from H2
                );
            }
        };
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    private Optional<com.example.portal.model.User> createUserIfNotExist(String username, Set<Role> roles) {
        if (!userRepository.existsById(username)) {
            com.example.portal.model.User user = new com.example.portal.model.User(username, roles);
            userRepository.save(user);
            return Optional.of(user);
        }else{
            return userRepository.findByUsername(username);
        }
    }
}