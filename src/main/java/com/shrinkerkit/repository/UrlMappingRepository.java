package com.shrinkerkit.repository;

import com.shrinkerkit.entity.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for the UrlMapping entity.
 * This interface provides CRUD (Create, Read, Update, Delete) operations
 * and allows for defining custom query methods.
 */
@Repository
public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long> {

    /**
     * Checks if a UrlMapping with the given short code exists in the database.
     * Spring Data JPA automatically implements this method based on its name.
     *
     * @param shortCode The short code to check for.
     * @return true if an entry exists, false otherwise.
     */
    boolean existsByShortCode(String shortCode);

    /**
     * Finds a UrlMapping entity by its short code.
     * This will be used for the redirection logic.
     *
     * @param shortCode The short code to search for.
     * @return an Optional containing the UrlMapping if found, or an empty Optional otherwise.
     */
    Optional<UrlMapping> findByShortCode(String shortCode);
}