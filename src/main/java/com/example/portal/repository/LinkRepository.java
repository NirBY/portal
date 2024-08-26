package com.example.portal.repository;

import com.example.portal.model.Link;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LinkRepository extends JpaRepository<Link, Long> {

    // Find links by title
    List<Link> findByTitle(String title);

    // Find links by URL
    List<Link> findByUrl(String url);

    // Find all links that belong to a specific data center
    List<Link> findByDataCenterId(Long dataCenterId);

    // Find links by title containing a specific string (case insensitive)
    List<Link> findByTitleContainingIgnoreCase(String title);

    // Find links by description containing a specific string (case insensitive)
    List<Link> findByDescriptionContainingIgnoreCase(String description);
}
