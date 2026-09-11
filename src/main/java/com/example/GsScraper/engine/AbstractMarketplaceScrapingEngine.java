package com.example.GsScraper.engine;

import com.example.GsScraper.mapper.ListingMapper;
import com.example.GsScraper.model.dto.ListingDto;
import com.example.GsScraper.model.entity.ListingEntity;
import com.example.GsScraper.model.entity.SearchKeywordEntity;
import com.example.GsScraper.model.enumerated.Marketplace;
import com.example.GsScraper.repository.ListingRepository;
import com.example.GsScraper.repository.SearchKeywordRepository;
import com.example.GsScraper.service.notification.TelegramNotifier;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public abstract class AbstractMarketplaceScrapingEngine implements MarketplaceScrapingEngine {

    protected final SearchKeywordRepository searchKeywordRepository;
    protected final ListingRepository listingRepository;
    protected final TelegramNotifier telegramNotifier;
    protected final ListingMapper listingMapper;

    protected AbstractMarketplaceScrapingEngine(SearchKeywordRepository searchKeywordRepository,
                                                ListingRepository listingRepository,
                                                TelegramNotifier telegramNotifier,
                                                ListingMapper listingMapper) {
        this.searchKeywordRepository = searchKeywordRepository;
        this.listingRepository = listingRepository;
        this.telegramNotifier = telegramNotifier;
        this.listingMapper = listingMapper;
    }

    @Override
    public void runScrapingCycle() {
        List<String> keywords = getKeywords();

        for (String keyword : keywords) {
            scrapeKeyword(keyword);
            waitBetweenFetches();
        }
    }

    protected List<String> getKeywords() {
        return searchKeywordRepository.findByMarketplace(getMarketplace())
                .stream()
                .map(SearchKeywordEntity::getKeyword)
                .toList();
    }

    protected void scrapeKeyword(String keyword) {
        List<ListingDto> foundListings = fetchAndFilter(keyword);
        System.out.println("Tisztított találatok (címben szerepel a keyword): " + foundListings.size());

        List<ListingDto> newListings = foundListings.stream()
                .filter(dto -> !listingRepository.existsByMarketplaceAndUrl(getMarketplace(), dto.getUrl()))
                .toList();
        System.out.println("Új találat ezek közül: " + newListings.size());


        if (!newListings.isEmpty()) {
            List<ListingEntity> entities = newListings.stream()
                    .map(dto -> listingMapper.toEntity(dto, getMarketplace()))
                    .toList();
            entities.forEach(listingEntity -> listingEntity.setActive(true));
            listingRepository.saveAll(entities);
            sendNewListingNotifications(keyword, newListings);
        }
    }

    protected void sendNewListingNotifications(String keyword, List<ListingDto> newListings) {
        telegramNotifier.sendSimpleMessage(createNewListingNotification(keyword, getMarketplace()));
        System.out.println(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + " - TELEGRAM: sending notifications about new items (" + newListings.size() + ")");
        newListings.forEach(telegramNotifier::sendInstrumentNotification);
    }

    @Override
    public boolean isSummaryReportsEnabled() {
        return false;
    }

    @Override
    public void sendMorningBriefing() {
        if (!isSummaryReportsEnabled()) {
            return;
        }
        telegramNotifier.sendSimpleMessage(createBriefingMessage("REGGELI JELENTÉS"));
        sendSummary();
    }

    @Override
    public void sendEveningBriefing() {
        if (!isSummaryReportsEnabled()) {
            return;
        }
        telegramNotifier.sendSimpleMessage(createBriefingMessage("ESTI JELENTÉS"));
        sendSummary();
    }

    protected void sendSummary() {
        List<ListingDto> listings = listingRepository.findByMarketplace(getMarketplace())
                .stream()
                .map(listingMapper::toDto)
                .toList();

        if (listings.isEmpty()) {
            telegramNotifier.sendSimpleMessage("Nincs jelenleg eltárolt találat ehhez: " + getMarketplace());
            return;
        }

        listings.forEach(telegramNotifier::sendInstrumentNotification);
    }

    protected abstract List<ListingDto> fetchAndFilter(String keyword);

    protected abstract void waitBetweenFetches();

    private String createBriefingMessage(String title) {
        return "\n<b>"+title+" - " + getMarketplace() + "</b>\n";
    }

    private String createNewListingNotification(String keyword, Marketplace marketplace) {
        return "\n<b>\uD83D\uDD14 ÚJ HIRDETÉS - " + marketplace + "</b>\n" + keyword + "\n";
    }
}