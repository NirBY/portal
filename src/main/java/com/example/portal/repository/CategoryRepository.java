package com.example.portal.repository;

import com.example.portal.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.RepositoryDefinition;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RepositoryDefinition(domainClass = Category.class, idClass = Long.class)
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByRank(int rank);

    List<Category> findAllByOrderByRankAsc();

    // Find a category by its name
    Optional<Category> findByName(String name);

    // Find all categories that contain a specific string in their name (case insensitive)
    List<Category> findByNameContainingIgnoreCase(String name);

    // Find categories with a name starting with a specific prefix
    List<Category> findByNameStartingWith(String prefix);

}