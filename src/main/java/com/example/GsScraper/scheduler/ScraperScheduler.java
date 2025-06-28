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
import java.util.stream.Collectors;

@Component
public class ScraperScheduler {

    @Autowired
    private InstrumentRepository instrumentRepository;

    @Autowired
    private TelegramNotifier telegramNotifier;

    @Autowired
    private GsSearchKeywordRepository gsSearchKeywordRepository;

    @Autowired
    private InstrumentScraperService scraperService;

    static final int minutes = 60 * 1000;


    @Scheduled(fixedRate = 30 * minutes)
    public void scheduledFetch() {

        List<String> keywordsList = getAllKeywordsFromDb();

        keywordsList.forEach(
                currentKeyword -> {

                    //get new instruments by keyword
                    List<InstrumentEntity> newInstruments = getInstrumentEntities(currentKeyword);

                    //persist new instruments
                    instrumentRepository.saveAll(newInstruments);

                    //notify user
                    sendNewInstrumentNotificationToUser(
                            InstrumentMapper.toDtos(newInstruments),
                            currentKeyword
                    );
                }
        );
    }

    private List<InstrumentEntity> getInstrumentEntities(String currentKeyword) {
        String[] keywordArray = extractSingleKeywordsOrPhreses(currentKeyword);
        String cleanedKeyword = currentKeyword.replaceAll("&", " ");

        return fetchNewInstruments(cleanedKeyword)
                .stream()
                .filter(instrument -> containsAll(instrument.getTitle(), keywordArray))
                .collect(Collectors.toList());
    }

    private static String[] extractSingleKeywordsOrPhreses(String keyword) {
        String[] keywords = keyword.split("$");
        for (int i = 0; i < keywords.length; i++) {
            keywords[i] = keywords[i].trim();
        }
        return keywords;
    }

    private boolean containsAll(String title, String[] keywords) {
        boolean result = true;
        for (String keyword : keywords) {
            if (!title.toLowerCase().contains(keyword.toLowerCase())) {
                result = false;
            }
        }
        return result;
    }

    @Scheduled(cron = "0 0 21 * * *")
    public void sendEveningBriefing() {
        sendBriefingWithMessage("ESTI JELENTÉS");
    }

    @Scheduled(cron = "0 0 8 * * *")
    public void sendMorningBriefing() {
        sendBriefingWithMessage("REGGELI JELENTÉS");
    }

    private void sendBriefingWithMessage(String message) {
        List<InstrumentDto> allInstruments = InstrumentMapper.toDtos(instrumentRepository.findAll());
        telegramNotifier.sendSimpleMessage("\n"+message+": \n");
        sendSummary(allInstruments);
    }

    private List<InstrumentEntity> fetchNewInstruments(String keyword) {
        List<InstrumentEntity> allMatchingInstruments = getAllMatchingInstruments(keyword);
        return getNewInstruments(allMatchingInstruments);
    }

    private List<InstrumentEntity> getAllMatchingInstruments(String keyword) {
        List<InstrumentEntity> allMatchingInstruments;
        try {
            allMatchingInstruments = new ArrayList<>(scraperService.fetchInstruments(keyword));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return allMatchingInstruments;
    }

    private List<String> getAllKeywordsFromDb() {
        List<String> keywords = new ArrayList<>();
        gsSearchKeywordRepository.findAll().forEach(keyword -> {
            keywords.add(keyword.getKeyword());
        });
        return keywords;
    }


    private void sendNewInstrumentNotificationToUser(List<InstrumentDto> newInstruments, String keyword) {
        if (!newInstruments.isEmpty()) {
            telegramNotifier.sendSimpleMessage("\n<b>!!! ÚJ HIRDETÉS EBBEN A KATEGÓRIÁBAN: </b> \n " + keyword + "\n");
            newInstruments.forEach(
                    instrumentDto -> telegramNotifier.sendNewInstrumentNotification(instrumentDto)
            );
        } else {
            System.out.println("Nincs uj hangszer ebben a kategóriában: " + keyword);
        }
    }

    private void sendSummary(List<InstrumentDto> newInstruments) {
        if (!newInstruments.isEmpty()) {
            newInstruments.forEach(instrumentDto -> telegramNotifier.sendNewInstrumentNotification(instrumentDto)
            );
        } else {
            System.out.println("A KERESÉSI LISTA JELENLEG TELJESEN ÜRES");
        }
    }

    private List<InstrumentEntity> getNewInstruments(List<InstrumentEntity> instruments) {
        List<InstrumentEntity> newInstruments = new ArrayList<>();

        instruments.forEach(
                instrumentEntity -> {
                    if (!instrumentRepository.existsByTitlePictureURL(instrumentEntity.getTitlePictureURL())) {
                        newInstruments.add(instrumentEntity);
                    }
                }
        );
        return newInstruments;
    }
}

