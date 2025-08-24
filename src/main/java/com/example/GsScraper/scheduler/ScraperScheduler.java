package com.example.GsScraper.scheduler;


import com.example.GsScraper.exception.HumanVerificationException;
import com.example.GsScraper.model.InstrumentDto;
import com.example.GsScraper.model.InstrumentEntity;
import com.example.GsScraper.model.InstrumentMapper;
import com.example.GsScraper.repository.GsSearchKeywordRepository;
import com.example.GsScraper.repository.InstrumentRepository;
import com.example.GsScraper.service.InstrumentScraperService;
import com.example.GsScraper.service.TelegramNotifier;
import com.example.GsScraper.utils.ScraperUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


import java.io.IOException;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ScraperScheduler {

    private static final int TIMEOUT_RETRY_AFTER_SECONDS = 5;
    private static final int MINUTES = 60 * 1000;
    private static final LocalTime SCRAPING_SERVICE_START_TIME = LocalTime.of(7, 30);
    private static final LocalTime SCRAPING_SERVICE_END_TIME = LocalTime.of(22, 30);
    public static int SCRAPE_RATE_LIMIT_SECONDS = 20;
    public static int SCRAPE_RATE_LIMIT_RAISE_STEP_INTERVAL = 5;
    private static int SCRAPE_TRY_COUNTER = 0;
    private static boolean IS_SCRAPING_OFF_HOURS_NOTIFICATION_SENT = false;

    @Autowired
    private InstrumentRepository instrumentRepository;

    @Autowired
    private TelegramNotifier telegramNotifier;

    @Autowired
    private GsSearchKeywordRepository gsSearchKeywordRepository;

    @Autowired
    private InstrumentScraperService scraperService;

    @Scheduled(fixedRate = 30 * MINUTES, initialDelay = 5_000)
    public void scheduledFetch() {


        if (isBetween(SCRAPING_SERVICE_START_TIME, SCRAPING_SERVICE_END_TIME)) {
            IS_SCRAPING_OFF_HOURS_NOTIFICATION_SENT = true;
            startScheduledFetchAndNotification();
        } else {
            sendScrapingIsOffWorkingHoursNotification(SCRAPING_SERVICE_START_TIME, SCRAPING_SERVICE_END_TIME);
        }
    }

    @Scheduled(cron = "0 0 21 * * *")
    public void sendEveningBriefing() {
        sendBriefingWithMessage(ScraperUtils.getFormattedCurrentDateTime() + " - ESTI JELENTÉS");
    }

    @Scheduled(cron = "0 0 8 * * *")
    public void sendMorningBriefing() {
        sendBriefingWithMessage(ScraperUtils.getFormattedCurrentDateTime() + " - REGGELI JELENTÉS");
    }

    private void startScheduledFetchAndNotification() {

        sendScrapingStartedNotification();

        //Get data from db
        List<String> keywordsList = getAllKeywordsFromDb();
        List<InstrumentEntity> allActiveInstrumentsInDb = instrumentRepository.findByActiveTrue();

        //Scrape for instruments
        Map<String, List<InstrumentEntity>> allInstrumentsFound = null;
        try {
            allInstrumentsFound = fetchInstrumentsByKeywords(keywordsList);
        } catch (HumanVerificationException e) {
            raiseScrapeRateLimit();
            sendHumanVerificationErrorNotification();
            return;
        }
        Map<String, List<InstrumentEntity>> newInstrumentsMap = addNewInstruments(allInstrumentsFound, allActiveInstrumentsInDb);

        //Save new instruments & notify
        if (!newInstrumentsMap.isEmpty()) {
            saveInstruments(mapToList(newInstrumentsMap));
            sendNotificationAboutNewInstruments(newInstrumentsMap);
        }

        //If an ad removed, mark inactive in db & notify
        List<InstrumentEntity> newlyInactiveInstruments = filterForNewInactiveInstruments(allActiveInstrumentsInDb, mapToList(allInstrumentsFound));
        if (!newlyInactiveInstruments.isEmpty()) {
            inactivateInDb(newlyInactiveInstruments);
            sendNotificationAboutNewInactiveInstruments(InstrumentMapper.toDtos(newlyInactiveInstruments));
        }
    }

    private void raiseScrapeRateLimit() {
        SCRAPE_RATE_LIMIT_SECONDS += SCRAPE_RATE_LIMIT_RAISE_STEP_INTERVAL;
    }

    private void inactivateInDb(List<InstrumentEntity> newlyInactiveInstruments) {
        setInactiveInstruments(newlyInactiveInstruments);
        saveInstruments(newlyInactiveInstruments);
    }

    private Map<String, List<InstrumentEntity>> fetchInstrumentsByKeywords(List<String> keywordsList)
            throws HumanVerificationException {

        Map<String, List<InstrumentEntity>> allInstrumentsFound = new LinkedHashMap<>();

        for (String keyword : keywordsList) {
            List<InstrumentEntity> instrumentsByKeyword = fetchInstrumentsMatching(keyword);
            if (!instrumentsByKeyword.isEmpty()) {
                allInstrumentsFound.put(keyword, instrumentsByKeyword);
            }
            ScraperUtils.waitForSecondsWithConsoleMessage(SCRAPE_RATE_LIMIT_SECONDS, "Scrape iteration limit: Várunk " + SCRAPE_RATE_LIMIT_SECONDS + " másodpercet a következő keresés előtt");
        }
        return allInstrumentsFound;
    }

    private Map<String, List<InstrumentEntity>> addNewInstruments(Map<String, List<InstrumentEntity>> allInstrumentsFound, List<InstrumentEntity> allInstrumentsInDb) {
        Map<String, List<InstrumentEntity>> newInstrumentsMap = new LinkedHashMap<>();
        for (String keyword : allInstrumentsFound.keySet()) {
            List<InstrumentEntity> newInstrumentsByKeyword = filterForNew(allInstrumentsFound.get(keyword), allInstrumentsInDb);
            if (!newInstrumentsByKeyword.isEmpty()) {
                newInstrumentsMap.put(keyword, newInstrumentsByKeyword);
            }
        }
        return newInstrumentsMap;
    }

    private static List<InstrumentEntity> mapToList(Map<String, List<InstrumentEntity>> newInstrumentsMap) {
        return newInstrumentsMap.values().stream().flatMap(Collection::stream).toList();
    }

    private void saveInstruments(List<InstrumentEntity> newInstrumentsMap) {
        instrumentRepository.saveAll(newInstrumentsMap);
    }

    private List<InstrumentEntity> fetchInstrumentsMatching(String currentKeyword) throws HumanVerificationException {

        //extracting keywords separated by & operator
        String[] keywordArray = extractSingleKeywordsOrPhrases(currentKeyword);

        //removing the & operator for the search itself
        String cleanedKeyword = currentKeyword.replaceAll("&", " ");

        return scrapeForInstruments(cleanedKeyword).stream().filter(instrument -> containsAll(instrument.getTitle(), keywordArray)).collect(Collectors.toList());
    }

    private void sendNotificationAboutNewInstruments(Map<String, List<InstrumentEntity>> newInstrumentsMap) {
        for (String keyword : newInstrumentsMap.keySet()) {
            sendNewInstrumentNotificationToUser(InstrumentMapper.toDtos(newInstrumentsMap.get(keyword)), keyword);
        }
    }

    private void sendBriefingWithMessage(String message) {
        List<InstrumentDto> allInstruments = InstrumentMapper.toDtos(instrumentRepository.findByActiveTrue());
        telegramNotifier.sendSimpleMessage("\n" + message + ": \n");
        sendSummary(allInstruments);
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


    private List<InstrumentEntity> scrapeForInstruments(String keyword) throws HumanVerificationException {

        List<InstrumentEntity> allMatchingInstruments = new ArrayList<>();
        try {
            allMatchingInstruments.addAll(scraperService.fetchInstruments(keyword));
        } catch (HumanVerificationException humanVerificationException) {
            throw humanVerificationException;
        } catch (IOException e) {
            System.err.println("Timeout ennél a kulcsszónál: " + keyword + " Megpróbálom még egyszer " + TIMEOUT_RETRY_AFTER_SECONDS + " másodperc múlva");
            if (SCRAPE_TRY_COUNTER == 0) {
                SCRAPE_TRY_COUNTER++;
                ScraperUtils.waitForSeconds(TIMEOUT_RETRY_AFTER_SECONDS);
                scrapeForInstruments(keyword);
            } else {
                System.err.println("Második timeout ennél a kulcsszónál: " + keyword + "Folytatom egy másik kulcsszóval " + TIMEOUT_RETRY_AFTER_SECONDS + " másodperc múlva");
                ScraperUtils.waitForSeconds(TIMEOUT_RETRY_AFTER_SECONDS);
                SCRAPE_TRY_COUNTER = 0;
            }
        }
        if (allMatchingInstruments.isEmpty()) {
            System.out.println("Nincs hangszer ezzel a kulcsszóval: " + keyword);
        } else {
            System.out.println("Összesen" + allMatchingInstruments.size() + " db hangszer ezzel a kulcsszóval: " + keyword);
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
        newInstruments.forEach(instrumentDto -> telegramNotifier.sendInstrumentNotification(instrumentDto));

    }

    private void sendNotificationAboutNewInactiveInstruments(List<InstrumentDto> dtos) {
        telegramNotifier.sendSimpleMessage("\n<b>!!! A következő hangszerek hirdetése inaktív lett: </b> \n ");
        dtos.forEach(instrumentDto -> telegramNotifier.sendInstrumentNotification(instrumentDto));
    }

    private void setInactiveInstruments(List<InstrumentEntity> newlyInactiveInstruments) {
        newlyInactiveInstruments.forEach(instrumentEntity -> {
            instrumentEntity.setActive(false);
        });
    }

    private void sendSummary(List<InstrumentDto> newInstruments) {
        if (!newInstruments.isEmpty()) {
            newInstruments.forEach(instrumentDto -> telegramNotifier.sendInstrumentNotification(instrumentDto));
        } else {
            System.out.println("A KERESÉSI LISTA JELENLEG TELJESEN ÜRES");
        }
    }

    private List<InstrumentEntity> filterForNew(List<InstrumentEntity> allInstrumentsFound, List<InstrumentEntity> allInstrumentsInDb) {
        List<InstrumentEntity> newInstruments = new ArrayList<>();
        allInstrumentsFound.forEach(instrumentEntity -> {
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
        });
        return newInstruments;
    }

    private List<InstrumentEntity> filterForNewInactiveInstruments(List<InstrumentEntity> allActiveInstrumentsInDb, List<InstrumentEntity> allInstrumentsFound) {
        List<InstrumentEntity> newInactiveInstruments = new ArrayList<>();

        allActiveInstrumentsInDb.forEach(instrumentInDb -> {
            String activeInstrumentInDB = instrumentInDb.getUrl();

            for (InstrumentEntity instrument : allInstrumentsFound) {
                if (instrument.getUrl().equals(activeInstrumentInDB)) {
                    break;
                }
            }

            // If there is an item that is marked active in db,
            // but the item ad has been removed, it is a newly inactivated item
            newInactiveInstruments.add(instrumentInDb);
        });

        return newInactiveInstruments;
    }

    private boolean isBetween(LocalTime time1, LocalTime time2) {
        LocalTime now = LocalTime.now();
        return !now.isBefore(time1) && !now.isAfter(time2);
    }

    private void sendScrapingStartedNotification() {
        telegramNotifier.sendSimpleMessage(
                String.format("\n<b>*** SCRAPING INDUL... RATE LIMIT: %d másodperc ***</b>\n", SCRAPE_RATE_LIMIT_SECONDS)
        );
    }


    private void sendScrapingIsOffWorkingHoursNotification(LocalTime time1, LocalTime time2) {
        if (!IS_SCRAPING_OFF_HOURS_NOTIFICATION_SENT) {
            telegramNotifier.sendSimpleMessage("Scraping stops after " + toRegularFormat(time2) + " Service starts tomorrow at: " + toRegularFormat(time1));
            IS_SCRAPING_OFF_HOURS_NOTIFICATION_SENT = true;
        }
    }

    private String toRegularFormat(LocalTime time) {
        return time.getHour() + ":" + time.getMinute();
    }

    private void sendHumanVerificationErrorNotification() {
        telegramNotifier.sendSimpleMessage(
                String.format("\n<b>*** HUMAN VERIFICATION ERROR! 30 perc múlva újra próbáljuk a kulcsszavak között %d másodperc időközökkel... ***</b>\n", SCRAPE_RATE_LIMIT_SECONDS)
        );
    }
}

