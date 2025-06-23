package com.example.GsScraper.scheduler;


import com.example.GsScraper.model.InstrumentDto;
import com.example.GsScraper.model.InstrumentEntity;
import com.example.GsScraper.model.InstrumentMapper;
import com.example.GsScraper.repository.GsSearchKeywordRepository;
import com.example.GsScraper.repository.InstrumentRepository;
import com.example.GsScraper.service.InstrumentScraperService;
import com.example.GsScraper.service.TelegramNotifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class ScraperScheduler {

    @Autowired
    InstrumentRepository instrumentRepository;

    @Autowired
    TelegramNotifier telegramNotifier;

    @Autowired
    GsSearchKeywordRepository gsSearchKeywordRepository;

    static final int minutes = 60 * 1000;

    private final InstrumentScraperService scraperService;

    public ScraperScheduler(InstrumentScraperService scraperService) {
        this.scraperService = scraperService;
    }

    @Scheduled(fixedRate = 30 * minutes)
    public void scheduledFetch() {
            List<String> keywords = new ArrayList<>();
            gsSearchKeywordRepository.findAll().forEach(keyword -> {keywords.add(keyword.getKeyword());});

            keywords.forEach( keyword -> {
                List<InstrumentEntity> instruments = null;
                try {
                    instruments = scraperService.fetchInstruments(keyword);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                List<InstrumentDto> newInstruments = getNewInstruments(instruments);
                sendNewInstrumentNotificationToUser(newInstruments, keyword);
            });
    }

    private void sendNewInstrumentNotificationToUser(List<InstrumentDto> newInstruments, String keyword) {
        if (!newInstruments.isEmpty()) {
            telegramNotifier.sendSimpleMessage("Új hirdetés ebben a kategóriában: " + keyword);
            newInstruments.forEach(
                    instrumentDto -> telegramNotifier.sendNewInstrumentNotification(instrumentDto)
            );
        } else {
            System.out.println("Nincs uj hangszer ebben a kategóriában: " + keyword);
        }
    }

    private List<InstrumentDto> getNewInstruments(List<InstrumentEntity> instruments) {
        List<InstrumentDto> newInstruments = new ArrayList<>();

        //checking for new instruments, saving them to db and adding them to notification list
        instruments.forEach(
                instrumentEntity -> {
                    if (!instrumentRepository.existsByTitlePictureURL(instrumentEntity.getTitlePictureURL())) {
                        instrumentRepository.save(instrumentEntity);
                        newInstruments.add(InstrumentMapper.toDto(instrumentEntity));
                    }
                }
        );
        return newInstruments;
    }
}

