package com.shrinkerkit.service;

import com.shrinkerkit.repository.UrlMappingRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.stream.Collectors;

/**
 * Service responsible for generating unique short codes for URLs.
 */
@Service
public class ShortCodeService {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int MAX_RETRIES = 10;

    @Value("${shrinkerkit.code.length:7}")
    private int codeLength;

    private final UrlMappingRepository urlMappingRepository;

    public ShortCodeService(UrlMappingRepository urlMappingRepository) {
        this.urlMappingRepository = urlMappingRepository;
    }

    private String generateRandomCode() {
        return RANDOM.ints(codeLength, 0, CHARACTERS.length())
                     .mapToObj(CHARACTERS::charAt)
                     .map(Object::toString)
                     .collect(Collectors.joining());
    }

    /**
     *
     * @return A unique short code.
     * @throws RuntimeException if a unique code cannot be generated after max retries.
     */
    public String generateUniqueShortCode() {
        int attempts = 0;
        while (attempts < MAX_RETRIES) {
            String code = generateRandomCode();
            if (!urlMappingRepository.existsByShortCode(code)) {
                return code;
            }
            attempts++;
        }
        throw new RuntimeException("Failed to generate a unique short code after " + MAX_RETRIES + " attempts.");
    }
}