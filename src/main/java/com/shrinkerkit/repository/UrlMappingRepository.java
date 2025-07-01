package com.shrinkerkit.repository;

import com.shrinkerkit.entity.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long> {
    boolean existsByShortCode(String shortCode);
    Optional<UrlMapping> findByShortCode(String shortCode);
}