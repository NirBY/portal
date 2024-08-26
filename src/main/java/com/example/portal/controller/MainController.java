package com.example.portal.controller;

import com.example.portal.model.Category;
import com.example.portal.repository.CategoryRepository;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class MainController {

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping("/")
    public String showIndexPage(Model model) {
        // Fetch all categories, order by rank, and initialize lazy collection of environments
        List<Category> categories = categoryRepository.findAllByOrderByRankAsc();
        // Manually initialize the lazy collection
        categories.forEach(category -> Hibernate.initialize(category.getEnvironments()));
        model.addAttribute("categories", categories);
        return "index"; // Return the name of the template (index.html)
    }
}