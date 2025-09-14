package com.example.GsScraper.service;

import com.example.GsScraper.exception.HumanVerificationException;
import com.example.GsScraper.model.InstrumentEntity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class SeleniumInstrumentScraperService {

    private static final String GS_FANATIC_BASE_SEARCH_URL =
            "https://gsfanatic.com/hu/search?choice=instrument&search_settings=de4ea6de49bf5d49f815e194b744b8f7&q=";

    private static WebDriver driver;
    private static WebDriverWait wait;

    public List<InstrumentEntity> fetchInstruments(String keyword) {

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

        if (driver == null) {
            driver = new ChromeDriver(options);
        }
        if (wait == null) {
            wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        }

        try {

            String initialUrl = GS_FANATIC_BASE_SEARCH_URL + keyword;
            System.out.println("URL: " + initialUrl);
            driver.get(initialUrl);

            WebElement verificationCheckbox = findHumanVerificationCheckbox();

            if (verificationCheckbox != null) {
                System.out.println("  -- WebDRIVER: Human verification!");
                handleHumanVerification(verificationCheckbox);
                waitForVerificationCompletionSimple(verificationCheckbox);
            } else {
                System.out.println("  -- WebDRIVER: NO Human verification");
            }

            Document doc = getPageSourceAsJsoupDocument();
            Elements cards = doc.select("div.card");
            return getInstrumentsFromCards(cards);


        } catch (Exception e) {
            throw new RuntimeException("Hiba a hangszerek lekérése során: " + e.getMessage(), e);
        }
    }

    private List<InstrumentEntity> getInstrumentsFromCards(Elements cards) {
        List<InstrumentEntity> instruments = new ArrayList<>();
        for (Element card : cards) {
            try {
                Element titleEl = card.selectFirst("h2.h6");
                Element priceEl = card.selectFirst("p.price_tag");
                Element imgEl = card.selectFirst("img");
                Element urlEl = card.selectFirst("a");

                if (titleEl == null || priceEl == null || imgEl == null || urlEl == null) {
                    continue;
                }

                String title = titleEl.text().trim();
                String priceText = priceEl.text().trim();
                String imgUrl = imgEl.absUrl("src");
                String url = urlEl.absUrl("href");


                instruments.add(new InstrumentEntity(
                        url,
                        LocalDate.now(),
                        title,
                        priceText,
                        imgUrl,
                        true
                ));
            } catch (Exception e) {
                System.err.println("Hirdetés feldolgozása sikertelen: " + e.getMessage());
            }
        }
        return instruments;
    }


    private Document getPageSourceAsJsoupDocument() {
        try {
            waitForPageToLoad();

            String pageSource = driver.getPageSource();

            if (pageSource.contains("verification") && pageSource.contains("checkbox")) {
                System.out.println("--  WARNING: Page source still contains verification elements");
            }

            // 4. Jsoup document létrehozása
            Document doc = Jsoup.parse(pageSource);

            // 5. Base URL beállítása a relatív linkekhez
            doc.setBaseUri(driver.getCurrentUrl());
            return doc;
        } catch (Exception e) {
            System.err.println("Error converting page source to Jsoup: " + e.getMessage());
            throw new RuntimeException("Failed to parse page content");
        }
    }

    private void waitForPageToLoad() {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(webDriver ->
                    ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete")
            );

            Thread.sleep(2000);

        } catch (Exception e) {
            System.err.println("Page load wait error: " + e.getMessage());
        }
    }

    private void waitForVerificationCompletionSimple(WebElement checkbox) {
        try {
            System.out.println("  -- WebDRIVER: Waiting after click...");

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

            wait.until(ExpectedConditions.or(
                    ExpectedConditions.invisibilityOf(checkbox),
                    ExpectedConditions.stalenessOf(checkbox),
                    ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.card, .product-item, .ad-item"))
            ));

            Thread.sleep(1500);

        } catch (Exception e) {
            System.err.println("  -- WebDRIVER: Verification wait error: " + e.getMessage());
        }
    }

    private WebElement findHumanVerificationCheckbox() {
        try {
            System.out.println("  -- WebDRIVER: Searching for human verification checkbox...");

            // Várakozás a JavaScript által létrehozott elemekre
            Thread.sleep(2500);

            // Elsődlegesen keresünk checkbox-ot type alapján
            String[] checkboxSelectors = {
                    "input[type='checkbox']",
                    "input.form-check-input",
                    "#checkbox-container input",
                    ".form-check input[type='checkbox']",
                    "input[type='checkbox'][name*='token']",
                    "input[type='checkbox'][id*='checkbox']"
            };

            for (String selector : checkboxSelectors) {
                try {
                    List<WebElement> checkboxes = driver.findElements(By.cssSelector(selector));
                    if (!checkboxes.isEmpty()) {
                        WebElement checkbox = checkboxes.get(0);
                        if (checkbox.isDisplayed() &&
                                "checkbox".equals(checkbox.getAttribute("type")) &&
                                checkbox.getAttribute("id") != null) {

                            return checkbox;
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error with selector " + selector + ": " + e.getMessage());
                }
            }
            return null;

        } catch (Exception e) {
            System.err.println("Error searching for verification checkbox: " + e.getMessage());
            return null;
        }
    }

    private void handleHumanVerification(WebElement checkbox) throws HumanVerificationException {
        try {

            simulateMouseMovements();


            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", checkbox);
            System.out.println("  -- WebDRIVER: Checkbox clicked");

            this.wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("input.form-check-input[type='checkbox']")));

            Thread.sleep(3000);

        } catch (Exception e) {
            throw new HumanVerificationException("-- WebDRIVER: Error during handling human verification: " + e.getMessage());
        }
    }

    private void simulateMouseMovements() {
        try {
            System.out.println("  -- WebDRIVER: Handling Human verification...");
            Actions actions = new Actions(driver);
            Random random = new Random();

            actions.moveToElement(driver.findElement(By.tagName("body")), 0, 0).perform();
            Thread.sleep(100);

            for (int i = 0; i < random.nextInt(3) + 3; i++) {
                int xOffset = random.nextInt(16) - 8;  // -8 to +8 pixel
                int yOffset = random.nextInt(16) - 8;  // -8 to +8 pixel
                actions.moveByOffset(xOffset, yOffset).perform();
                Thread.sleep(150 + random.nextInt(100)); // 150-250ms várakozás
            }

            System.out.println("  -- WEebDRIVER: Mouse simulation completed successfully");


        } catch (Exception e) {
            System.err.println("  -- WEebDRIVER: Mouse simulation error: " + e.getMessage());
        }
    }
}