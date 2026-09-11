package com.example.GsScraper.orchestrator;

import com.example.GsScraper.engine.MarketplaceScrapingEngine;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class ScrapingOrchestrator {

    private static final long ONE_HOUR_MILLIS = 60 * 60 * 1000L;

    private final List<MarketplaceScrapingEngine> scrapingEngines;

    public ScrapingOrchestrator(List<MarketplaceScrapingEngine> scrapingEngines) {
        this.scrapingEngines = new ArrayList<>(scrapingEngines);
        AnnotationAwareOrderComparator.sort(this.scrapingEngines);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void runAtStartup() {
        runAllScrapersSequentially();
    }

    @Scheduled(fixedRate = ONE_HOUR_MILLIS, initialDelay = ONE_HOUR_MILLIS)
    public void runPeriodically() {
        runAllScrapersSequentially();
    }

    @Scheduled(cron = "0 0 8 * * *", zone = "Europe/Budapest")
    public void sendMorningBriefings() {
        for (MarketplaceScrapingEngine engine : scrapingEngines) {
            try {
                engine.sendMorningBriefing();
            } catch (Exception e) {
                System.err.println("Morning briefing failed for " + engine.getMarketplace() + ": " + e.getMessage());
            }
        }
    }

    @Scheduled(cron = "0 0 21 * * *", zone = "Europe/Budapest")
    public void sendEveningBriefings() {
        for (MarketplaceScrapingEngine engine : scrapingEngines) {
            try {
                engine.sendEveningBriefing();
            } catch (Exception e) {
                System.err.println("Evening briefing failed for " + engine.getMarketplace() + ": " + e.getMessage());
            }
        }
    }

    private void runAllScrapersSequentially() {
        System.out.println("\n\n***************** SCRAPING ORCHESTRATOR: Starting scraping cycle...   *****************");

        for (MarketplaceScrapingEngine engine : scrapingEngines) {
            try {
                System.out.println("\n" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + " - SCRAPING ORCHESTRATOR: Starting marketplace: " + engine.getMarketplace());

                if (engine.isEnabledAtCurrentTime()) {
                    engine.runScrapingCycle();
                } else {
                    System.out.println("SCRAPING ORCHESTRATOR: Skipped due to time window: " + engine.getMarketplace());
                }

                System.out.println("\n" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + " - SCRAPING ORCHESTRATOR: Finished marketplace: " + engine.getMarketplace());

            } catch (Exception e) {
                System.err.println("SCRAPING ORCHESTRATOR: Error while scraping " + engine.getMarketplace() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("SCRAPING ORCHESTRATOR: Scraping cycle finished.");
    }
}