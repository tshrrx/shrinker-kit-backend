package com.shrinkerkit.shrinker_kit_backend.service;

import com.shrinkerkit.repository.UrlMappingRepository;
import com.shrinkerkit.service.ShortCodeService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the ShortCodeService.
 */
@ExtendWith(MockitoExtension.class)
class ShortCodeServiceTest {

    // Mock the repository to isolate the service from the database layer
    @Mock
    private UrlMappingRepository urlMappingRepository;

    // Inject the mock into our service instance
    @InjectMocks
    private ShortCodeService shortCodeService;

    @BeforeEach
    void setUp() {
        // Set the code length property for the service before each test
        ReflectionTestUtils.setField(shortCodeService, "codeLength", 7);
    }

    @Test
    void whenCodeIsUnique_thenReturnsCode() {
        // Arrange: Configure the mock repository to report that the code does not exist
        when(urlMappingRepository.existsByShortCode(anyString())).thenReturn(false);

        // Act: Generate a unique short code
        String code = shortCodeService.generateUniqueShortCode();

        // Assert: Verify the code is not null and has the correct length
        assertNotNull(code);
        assertEquals(7, code.length());
    }

    @Test
    void whenCollisionOccurs_thenRetriesAndSucceeds() {
        // Arrange: Simulate one collision.
        // The first time the repo is called, return true (collision).
        // The second time, return false (unique).
        when(urlMappingRepository.existsByShortCode(anyString()))
                .thenReturn(true)
                .thenReturn(false);

        // Act: Generate the code
        String code = shortCodeService.generateUniqueShortCode();

        // Assert: Verify the code is not null and has the correct length
        assertNotNull(code);
        assertEquals(7, code.length());
    }

    @Test
    void whenMaxRetriesExceeded_thenThrowsException() {
        // Arrange: Configure the mock repository to always report a collision
        when(urlMappingRepository.existsByShortCode(anyString())).thenReturn(true);

        // Act & Assert: Verify that a RuntimeException is thrown when we can't find a unique code
        assertThrows(RuntimeException.class, () -> {
            shortCodeService.generateUniqueShortCode();
        });
    }
}