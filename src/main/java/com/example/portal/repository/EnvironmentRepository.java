package com.example.portal.repository;

import com.example.portal.model.Environment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnvironmentRepository extends JpaRepository<Environment, Long> {

    // Find environments by name
    List<Environment> findByName(String name);

    // Find all environments that belong to a specific category
    List<Environment> findByCategoryId(Long categoryId);

    // Find environments by name containing a specific string (case insensitive)
    List<Environment> findByNameContainingIgnoreCase(String name);

}
