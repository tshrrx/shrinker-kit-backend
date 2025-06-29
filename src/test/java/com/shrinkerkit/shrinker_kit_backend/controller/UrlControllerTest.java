package com.shrinkerkit.shrinker_kit_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shrinkerkit.dto.UrlShortenRequest;
import com.shrinkerkit.entity.UrlMapping;
import com.shrinkerkit.repository.UrlMappingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the UrlController.
 */
@SpringBootTest
@AutoConfigureMockMvc
class UrlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UrlMappingRepository urlMappingRepository;

    // We mock Redis interactions in the controller test to keep it simple and fast.
    @MockBean
    private RedisTemplate<String, String> redisTemplate;

    @MockBean
    private ValueOperations<String, String> valueOperations;

    @BeforeEach
    void setUp() {
        // Clear the H2 database before each test
        urlMappingRepository.deleteAll();
        // Setup mock for RedisTemplate to prevent real connections
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void whenShortenUrl_thenReturnsCreatedAndShortUrl() throws Exception {
        UrlShortenRequest request = new UrlShortenRequest();
        request.setLongUrl("https://example.com/very/long/path");

        mockMvc.perform(post("/api/v1/urls")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortUrl", startsWith("http://localhost:8080/")));
    }

    @Test
    void whenRedirectToLongUrl_givenValidShortCode_thenRedirects() throws Exception {
        // Arrange: Create a URL mapping directly in the repository for a predictable test
        String longUrl = "https://www.google.com";
        UrlMapping mapping = new UrlMapping(longUrl, "test123");
        urlMappingRepository.save(mapping);

        // Act & Assert: Perform a GET request and expect a 302 redirect
        mockMvc.perform(get("/test123"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", longUrl));
    }
    
    @Test
    void whenRedirectToLongUrl_givenInvalidShortCode_thenReturnsNotFound() throws Exception {
        // Act & Assert: Perform a GET request with a code that doesn't exist
        mockMvc.perform(get("/invalidcode"))
                .andExpect(status().isNotFound());
    }
}