package com.shrinkerkit.controller;

import com.shrinkerkit.dto.UrlShortenRequest;
import com.shrinkerkit.dto.UrlShortenResponse;
import com.shrinkerkit.entity.UrlMapping;
import com.shrinkerkit.repository.UrlMappingRepository;
import com.shrinkerkit.service.ShortCodeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.net.URL;

@RestController
@RequestMapping("/api/v1")
public class UrlController {

    private final ShortCodeService shortCodeService;
    private final UrlMappingRepository urlMappingRepository;

    @Value("${shrinkerkit.base.url}")
    private String baseUrl;

    public UrlController(ShortCodeService shortCodeService, UrlMappingRepository urlMappingRepository) {
        this.shortCodeService = shortCodeService;
        this.urlMappingRepository = urlMappingRepository;
    }

    /**
     * Endpoint to create a new short URL.
     * @param request The request body containing the long URL to shorten.
     * @return A response entity containing the full short URL.
     */
    @PostMapping("/urls")
    public ResponseEntity<UrlShortenResponse> shortenUrl(@Valid @RequestBody UrlShortenRequest request) {
        // Generate a unique short code for the URL
        String code = shortCodeService.generateUniqueShortCode();

        // New UrlMapping entity and save it to the database
        UrlMapping urlMapping = new UrlMapping(request.getLongUrl(), code);
        urlMappingRepository.save(urlMapping);

        // Full short URL using the base URL
        String fullShortUrl = baseUrl + "/" + code;

        // Response DTO with a 201 status
        UrlShortenResponse response = new UrlShortenResponse(fullShortUrl);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}