package com.example.portal.controller;

import com.example.portal.model.Role;
import com.example.portal.model.User;
import com.example.portal.repository.RoleRepository;
import com.example.portal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/admin/users")
@Secured("ROLE_ADMIN")
public class UserRoleController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "userManagement";
    }

    @GetMapping("/{username}")
    public String getUserRoles(@PathVariable("username") String username, Model model) {
        Optional<User> userOptional = userRepository.findById(username);
        if (userOptional.isPresent()) {
            model.addAttribute("user", userOptional.get());
            model.addAttribute("roles", roleRepository.findAll());
        }
        return "editUserRoles";
    }

    @PostMapping("/{username}/addRole")
    public String addRole(@PathVariable("username") String username, @RequestParam("roleId") Long roleId, RedirectAttributes redirectAttributes) {
        Optional<User> userOptional = userRepository.findById(username);
        Optional<Role> roleOptional = roleRepository.findById(roleId);

        if (userOptional.isPresent() && roleOptional.isPresent()) {
            User user = userOptional.get();
            Role role = roleOptional.get();
            user.getRoles().add(role);
            userRepository.save(user);
        }
        return "redirect:/admin/users/" + username;
    }

    @PostMapping("/{username}/removeRole")
    public String removeRole(@PathVariable String username, @RequestParam Long roleId, RedirectAttributes redirectAttributes) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));

        if ("ROLE_ADMIN".equals(role.getName())) {
            redirectAttributes.addFlashAttribute("error", "You cannot remove the ADMIN role.");
            return "redirect:/admin/users/" + username;
        }

        user.getRoles().remove(role);
        userRepository.save(user);

        return "redirect:/admin/users/" + username;
    }
}
