package com.example.GsScraper.builder;

import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class HardveraproSearchUrlBuilder implements SearchUrlBuilder {

    @Override
    public String buildSearchUrl(String baseUrl, String keyword) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("Base URL cannot be null or empty");
        }

        if (keyword == null || keyword.isBlank()) {
            throw new IllegalArgumentException("Keyword cannot be null or empty");
        }

        String normalizedBaseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        String encodedKeyword = encode(keyword.trim());

        return normalizedBaseUrl +
                "keres.php?stext=" + encodedKeyword +
                "&stcid_text=&stcid=" +
                "&stmid_text=&stmid=" +
                "&minprice=&maxprice=" +
                "&cmpid_text=&cmpid=" +
                "&usrid_text=&usrid=" +
                "&__buying=1&__buying=0" +
                "&stext_none=" +
                "&__brandnew=1&__brandnew=0";
    }

    private String encode(String input) {
        return URLEncoder.encode(input, StandardCharsets.UTF_8);
    }
}