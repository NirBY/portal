
package com.example.portal.controller;

import com.example.portal.model.Category;
import com.example.portal.model.DataCenter;
import com.example.portal.model.Environment;
import com.example.portal.model.Link;
import com.example.portal.repository.CategoryRepository;
import com.example.portal.repository.DataCenterRepository;
import com.example.portal.repository.EnvironmentRepository;
import com.example.portal.repository.LinkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EnvironmentRepository environmentRepository;

    @Autowired
    private DataCenterRepository dataCenterRepository;

    @Autowired
    private LinkRepository linkRepository;

    @GetMapping
    public String showAdminPage(Model model) {
        //List<Category> categories = categoryRepository.findAll();
        // Fetch all categories, order by rank, and initialize lazy collection of environments
        List<Category> categories = categoryRepository.findAllByOrderByRankAsc();
        model.addAttribute("categories", categories);
        model.addAttribute("newCategory", new Category());
        model.addAttribute("newEnvironment", new Environment());
        model.addAttribute("newDataCenter", new DataCenter());
        model.addAttribute("newLink", new Link());
        return "admin";
    }

    @PostMapping("/categories")
    public String addOrUpdateCategory(@ModelAttribute("newCategory") Category category) {
        categoryRepository.save(category);
        return "redirect:/admin";
    }

    @PostMapping("/environments")
    public String addOrUpdateEnvironment(@RequestParam("categoryId") Long categoryId, @ModelAttribute("newEnvironment") Environment environment) {
        Optional<Category> categoryOptional = categoryRepository.findById(categoryId);
        if (categoryOptional.isPresent()) {
            Category category = categoryOptional.get();
            environment.setCategory(category);
            environmentRepository.save(environment);
        }
        return "redirect:/admin";
    }

    @PostMapping("/datacenters")
    public String addOrUpdateDataCenter(@RequestParam("environmentId") Long environmentId, @ModelAttribute("newDataCenter") DataCenter dataCenter) {
        Optional<Environment> environmentOptional = environmentRepository.findById(environmentId);
        if (environmentOptional.isPresent()) {
            Environment environment = environmentOptional.get();
            dataCenter.setEnvironment(environment);
            dataCenterRepository.save(dataCenter);
        }
        return "redirect:/admin";
    }

    @PostMapping("/links")
    public String addOrUpdateLink(@RequestParam("dataCenterId") Long dataCenterId, @ModelAttribute("newLink") Link link) {
        Optional<DataCenter> dataCenterOptional = dataCenterRepository.findById(dataCenterId);
        if (dataCenterOptional.isPresent()) {
            DataCenter dataCenter = dataCenterOptional.get();
            link.setDataCenter(dataCenter);
            linkRepository.save(link);
        }
        return "redirect:/admin";
    }

    @GetMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable("id") Long id) {
        categoryRepository.deleteById(id);
        return "redirect:/admin";
    }

    @GetMapping("/links/delete/{id}")
    public String deleteLink(@PathVariable("id") Long id) {
        linkRepository.deleteById(id);
        return "redirect:/admin";
    }

    @GetMapping("/environments/delete/{id}")
    public String deleteEnvironment(@PathVariable("id") Long id) {
        environmentRepository.deleteById(id);
        return "redirect:/admin";
    }

    @GetMapping("/datacenters/delete/{id}")
    public String deleteDatacenter(@PathVariable("id") Long id) {
        dataCenterRepository.deleteById(id);
        return "redirect:/admin";
    }

    @PostMapping("/categories/rank/increase")
    public String increaseRank(@RequestParam("categoryId") Long categoryId) {
        changeRank(categoryId, -1);  // Decrease rank value
        return "redirect:/admin";
    }

    @PostMapping("/categories/rank/decrease")
    public String decreaseRank(@RequestParam("categoryId") Long categoryId) {
        changeRank(categoryId, 1);  // Increase rank value
        return "redirect:/admin";
    }

    private void changeRank(Long categoryId, int delta) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid category Id:" + categoryId));

        logger.trace("Category:" + categoryId + ", Rank:" + category.getRank());

        int newRank = category.getRank() + delta;
        // Verify the rank is not less than 1
        if (newRank < 1) {
            newRank = 1;  // Optionally, reset to rank 1 or skip the operation
        }

        category.setRank(newRank);
        categoryRepository.save(category);
    }

    // Expose the list of categories for other controllers to access
    public List<Category> getCategories() {
        return categoryRepository.findAll();
    }
}