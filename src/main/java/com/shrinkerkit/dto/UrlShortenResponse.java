package com.shrinkerkit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UrlShortenResponse {
    private String shortUrl;
}