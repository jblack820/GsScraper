package com.example.GsScraper.scraper;

import com.example.GsScraper.model.dto.ListingDto;
import com.example.GsScraper.model.enumerated.Marketplace;

import java.util.List;

public interface MarketplaceScraper {
    Marketplace getMarketplace();
    List<ListingDto> fetchListings(String keyword);
}
