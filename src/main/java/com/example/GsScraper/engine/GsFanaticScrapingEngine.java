package com.example.GsScraper.engine;

import com.example.GsScraper.config.MarketplaceConfigProperties;
import com.example.GsScraper.config.MarketplaceProperties;
import com.example.GsScraper.mapper.ListingMapper;
import com.example.GsScraper.model.dto.ListingDto;
import com.example.GsScraper.model.enumerated.Marketplace;
import com.example.GsScraper.repository.ListingRepository;
import com.example.GsScraper.repository.SearchKeywordRepository;
import com.example.GsScraper.scraper.GsFanaticSeleniumScraper;
import com.example.GsScraper.service.notification.TelegramNotifier;
import com.example.GsScraper.util.ScraperUtils;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;

@Service
public class GsFanaticScrapingEngine extends AbstractMarketplaceScrapingEngine {

    private final GsFanaticSeleniumScraper scraper;
    private final MarketplaceConfigProperties marketplaceConfigProperties;

    public GsFanaticScrapingEngine(SearchKeywordRepository searchKeywordRepository,
                                   ListingRepository listingRepository,
                                   TelegramNotifier telegramNotifier,
                                   ListingMapper listingMapper,
                                   GsFanaticSeleniumScraper scraper,
                                   MarketplaceConfigProperties marketplaceConfigProperties) {
        super(searchKeywordRepository, listingRepository, telegramNotifier, listingMapper);
        this.scraper = scraper;
        this.marketplaceConfigProperties = marketplaceConfigProperties;
    }

    @Override
    public Marketplace getMarketplace() {
        return Marketplace.GSFANATIC;
    }

    @Override
    public boolean isEnabledAtCurrentTime() {
        if (!properties().isOnlyDaytime()) {
            return true;
        }

        LocalTime now = LocalTime.now();
        return !now.isBefore(properties().getStartTime())
                && !now.isAfter(properties().getEndTime());
    }

    @Override
    public boolean isSummaryReportsEnabled() {
        return properties().isSummaryReports();
    }

    @Override
    protected List<ListingDto> fetchAndFilter(String keyword) {
        String[] parts = keyword.split("&");
        String cleanedKeyword = keyword.replace("&", "+");

        return scraper.fetchListings(cleanedKeyword).stream()
                .filter(dto -> containsAllKeywords(dto.getTitle(), parts))
                .toList();
    }

    @Override
    protected void waitBetweenFetches() {
        ScraperUtils.waitForSeconds(properties().getInterFetchSeconds());
    }

    private MarketplaceProperties properties() {
        return marketplaceConfigProperties.getGsfanatic();
    }

    private boolean containsAllKeywords(String title, String[] keywords) {
        for (String keyword : keywords) {
            if (!title.toLowerCase().contains(keyword.trim().toLowerCase())) {
                return false;
            }
        }
        return true;
    }
}
