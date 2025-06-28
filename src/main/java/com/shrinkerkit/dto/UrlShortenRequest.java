package com.shrinkerkit.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
public class UrlShortenRequest {

    @NotBlank(message = "URL cannot be blank.")
    @URL(message = "A valid URL format is required.")
    private String longUrl;
}