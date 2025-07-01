package com.shrinkerkit.controller;

import com.shrinkerkit.dto.UrlShortenRequest;
import com.shrinkerkit.dto.UrlShortenResponse;
import com.shrinkerkit.entity.UrlMapping;
import com.shrinkerkit.repository.UrlMappingRepository;
import com.shrinkerkit.service.ShortCodeService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.concurrent.TimeUnit;

@RestController
public class UrlController {

    private static final Logger logger = LoggerFactory.getLogger(UrlController.class);

    private final ShortCodeService shortCodeService;
    private final UrlMappingRepository urlMappingRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${shrinkerkit.base.url}")
    private String baseUrl;

    public UrlController(ShortCodeService shortCodeService, UrlMappingRepository urlMappingRepository, RedisTemplate<String, String> redisTemplate) {
        this.shortCodeService = shortCodeService;
        this.urlMappingRepository = urlMappingRepository;
        this.redisTemplate = redisTemplate;
    }

    @PostMapping("/api/v1/urls")
    public ResponseEntity<UrlShortenResponse> shortenUrl(@Valid @RequestBody UrlShortenRequest request) {
        String code = shortCodeService.generateUniqueShortCode();

        UrlMapping urlMapping = new UrlMapping(request.getLongUrl(), code);
        urlMappingRepository.save(urlMapping);
        logger.info("Saved new mapping to database: {} -> {}", code, request.getLongUrl());

        redisTemplate.opsForValue().set(code, request.getLongUrl(), 24, TimeUnit.HOURS);
        logger.info("Saved new mapping to Redis cache: {}", code);

        String fullShortUrl = baseUrl + "/" + code;
        UrlShortenResponse response = new UrlShortenResponse(fullShortUrl);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirectToLongUrl(@PathVariable String shortCode) {
        String longUrl = redisTemplate.opsForValue().get(shortCode);

        if (longUrl != null) {
            logger.info("Cache HIT for code: '{}'. Redirecting to {}", shortCode, longUrl);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(longUrl))
                    .build();
        }
        logger.warn("Cache MISS for code: '{}'. Querying database.", shortCode);
        return urlMappingRepository.findByShortCode(shortCode)
                .map(urlMapping -> {
                    String foundUrl = urlMapping.getLongUrl();
                    redisTemplate.opsForValue().set(shortCode, foundUrl, 24, TimeUnit.HOURS);
                    logger.info("Populated cache for code: '{}'", shortCode);

                    return ResponseEntity.status(HttpStatus.FOUND)
                            .location(URI.create(foundUrl))
                            .<Void>build(); // FIX: Explicit type hint added here
                })
                .orElseGet(() -> {
                    logger.error("Code '{}' not found in database or cache.", shortCode);
                    return ResponseEntity.notFound().build();
                });
    }
}