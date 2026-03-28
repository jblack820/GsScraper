package com.example.GsScraper.engine;

import com.example.GsScraper.model.enumerated.Marketplace;

public interface MarketplaceScrapingEngine {
    Marketplace getMarketplace();
    void runScrapingCycle();
    boolean isEnabledAtCurrentTime();
    boolean isSummaryReportsEnabled();
    void sendMorningBriefing();
    void sendEveningBriefing();
}
