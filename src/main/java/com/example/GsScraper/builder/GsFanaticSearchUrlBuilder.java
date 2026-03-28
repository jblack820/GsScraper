package com.example.GsScraper.builder;

import org.springframework.stereotype.Service;

@Service
public class GsFanaticSearchUrlBuilder implements SearchUrlBuilder {

    @Override
    public String buildSearchUrl(String baseUrl, String keyword) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("Base URL cannot be null or empty");
        }

        if (keyword == null || keyword.isBlank()) {
            throw new IllegalArgumentException("Keyword cannot be null or empty");
        }

        return baseUrl + keyword.trim();
    }
}
