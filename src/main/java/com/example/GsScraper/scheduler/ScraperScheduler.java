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
import java.util.concurrent.atomic.AtomicBoolean;
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
        List<InstrumentEntity> allInstrumentsInDb = instrumentRepository.findAll();
        List<InstrumentEntity> allInstrumentsFound = new ArrayList<>();

        keywordsList.forEach(
                currentKeyword -> {

                    //scrape for new instruments
                    List<InstrumentEntity> allInstrumentMatchingKeyword = getAllMatchingInstruments(currentKeyword);
                    allInstrumentsFound.addAll(allInstrumentMatchingKeyword);
                    System.out.println("Matching instruments found: " + allInstrumentMatchingKeyword);
                    // save send notification about new instruments
                    List<InstrumentEntity> newInstruments = filterForNewInstruments(allInstrumentsInDb, allInstrumentMatchingKeyword);
                    if (!newInstruments.isEmpty()) {

                        instrumentRepository.saveAll(newInstruments);
                        sendNewInstrumentNotificationToUser(
                                InstrumentMapper.toDtos(newInstruments),
                                currentKeyword
                        );
                    }
                }
        );

        // inactivate and send notification about newly inactive instruments
        List<InstrumentEntity> newlyInactiveInstruments = filterForNewInactiveInstruments(allInstrumentsInDb, allInstrumentsFound);
        if (!newlyInactiveInstruments.isEmpty()) {
            setInactiveInstruments(newlyInactiveInstruments);
            instrumentRepository.saveAll(newlyInactiveInstruments);
            sendNewInactiveInstrumentNotificationToUser(
                    InstrumentMapper.toDtos(newlyInactiveInstruments));
        }
    }


    private List<InstrumentEntity> getAllMatchingInstruments(String currentKeyword) {

        //extracting keywords separated by & operator
        String[] keywordArray = extractSingleKeywordsOrPhrases(currentKeyword);

        //removing the & operator for the search itself
        String cleanedKeyword = currentKeyword.replaceAll("&", " ");

        return scrapeForInstruments(cleanedKeyword)
                .stream()
                .filter(instrument -> containsAll(instrument.getTitle(), keywordArray))
                .collect(Collectors.toList());
    }

    private static String[] extractSingleKeywordsOrPhrases(String keyword) {
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
        telegramNotifier.sendSimpleMessage("\n" + message + ": \n");
        sendSummary(allInstruments);
    }


    private List<InstrumentEntity> scrapeForInstruments(String keyword) {
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
        telegramNotifier.sendSimpleMessage("\n<b>!!! ÚJ HIRDETÉS EBBEN A KATEGÓRIÁBAN: </b> \n " + keyword + "\n");
        newInstruments.forEach(
                instrumentDto -> telegramNotifier.sendInstrumentNotification(instrumentDto)
        );

    }

    private void sendNewInactiveInstrumentNotificationToUser(List<InstrumentDto> dtos) {
        telegramNotifier.sendSimpleMessage("\n<b>!!! A következő hangszerek hirdetése inaktív lett: </b> \n ");
        dtos.forEach(
                instrumentDto -> telegramNotifier.sendInstrumentNotification(instrumentDto)
        );
    }

    private void setInactiveInstruments(List<InstrumentEntity> newlyInactiveInstruments) {
        newlyInactiveInstruments.forEach(instrumentEntity -> {
            instrumentEntity.setActive(false);
        });
    }

    private void sendSummary(List<InstrumentDto> newInstruments) {
        if (!newInstruments.isEmpty()) {
            newInstruments.forEach(instrumentDto -> telegramNotifier.sendInstrumentNotification(instrumentDto)
            );
        } else {
            System.out.println("A KERESÉSI LISTA JELENLEG TELJESEN ÜRES");
        }
    }

    private List<InstrumentEntity> filterForNewInstruments(List<InstrumentEntity> allInstrumentsInDb, List<InstrumentEntity> allInstrumentMatchingKeyword) {
        List<InstrumentEntity> newInstruments = new ArrayList<>();
        allInstrumentMatchingKeyword.forEach(instrumentEntity ->
                {
                    String instrumentAdURL = instrumentEntity.getUrl();
                    boolean isAlreadyInDb = false;
                    for (InstrumentEntity instrumentInDb : allInstrumentsInDb) {
                        if (instrumentInDb.getUrl().equals(instrumentAdURL)) {
                            isAlreadyInDb = true;
                            break;
                        }
                    }
                    if (!isAlreadyInDb) {
                        newInstruments.add(instrumentEntity);
                    }
                }
        );
        return newInstruments;
    }

    private List<InstrumentEntity> filterForNewInactiveInstruments(List<InstrumentEntity> allInstrumentsInDb, List<InstrumentEntity> allInstrumentMatchingKeyword) {
        List<InstrumentEntity> newInactiveInstruments = new ArrayList<>();

        allInstrumentsInDb.forEach(instrumentInDb -> {
            String instrumentURL = instrumentInDb.getUrl();
            AtomicBoolean isAdPresent = new AtomicBoolean(false);
            for (InstrumentEntity instrumentEntity : allInstrumentMatchingKeyword) {
                if (instrumentEntity.getUrl().equals(instrumentURL)) {
                    isAdPresent.set(true);
                    break;
                }
            }

            if (!isAdPresent.get()) {
                newInactiveInstruments.add(instrumentInDb);
            }
        });

        return newInactiveInstruments;
    }
}

