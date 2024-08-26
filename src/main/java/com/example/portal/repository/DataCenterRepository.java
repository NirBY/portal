package com.example.portal.repository;

import com.example.portal.model.DataCenter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DataCenterRepository extends JpaRepository<DataCenter, Long> {

    // Find data centers by name
    List<DataCenter> findByName(String name);

    // Find all data centers that belong to a specific environment
    List<DataCenter> findByEnvironmentId(Long environmentId);

    // Find data centers by name containing a specific string (case insensitive)
    List<DataCenter> findByNameContainingIgnoreCase(String name);

}
