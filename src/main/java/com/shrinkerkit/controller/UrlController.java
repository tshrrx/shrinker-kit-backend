package com.shrinkerkit.controller;

import com.shrinkerkit.dto.UrlShortenRequest;
import com.shrinkerkit.dto.UrlShortenResponse;
import com.shrinkerkit.entity.UrlMapping;
import com.shrinkerkit.repository.UrlMappingRepository;
import com.shrinkerkit.service.ShortCodeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/**
 * REST Controller for handling URL shortening and redirection requests.
 */
@RestController
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
     * Endpoint to create a new short URL. It is namespaced under /api/v1.
     * @param request The request body containing the long URL to shorten.
     * @return A response entity containing the full short URL.
     */
    @PostMapping("/api/v1/urls")
    public ResponseEntity<UrlShortenResponse> shortenUrl(@Valid @RequestBody UrlShortenRequest request) {
        String code = shortCodeService.generateUniqueShortCode();

        UrlMapping urlMapping = new UrlMapping(request.getLongUrl(), code);
        urlMappingRepository.save(urlMapping);

        String fullShortUrl = baseUrl + "/" + code;

        UrlShortenResponse response = new UrlShortenResponse(fullShortUrl);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Endpoint to redirect a short code to its original long URL.
     * This endpoint is at the root path to handle clicks on short links.
     * @param shortCode The short code from the URL path.
     * @return A 302 Found redirect to the long URL, or a 404 Not Found if the code is invalid.
     */
    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirectToLongUrl(@PathVariable String shortCode) {
        return urlMappingRepository.findByShortCode(shortCode)
                .map(urlMapping -> ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create(urlMapping.getLongUrl()))
                        .<Void>build()) // The build() call returns ResponseEntity<Void>
                .orElse(ResponseEntity.notFound().build());
    }
}
