package com.example.GsScraper.scraper;

import com.example.GsScraper.model.dto.ListingDto;
import com.example.GsScraper.util.SeleniumDriverFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

public abstract class AbstractSeleniumMarketplaceScraper implements MarketplaceScraper {
    protected WebDriver driver;
    protected WebDriverWait wait;

    @Override
    public List<ListingDto> fetchListings(String keyword) {
        initDriver();
        try {
            String searchUrl = buildSearchUrl(keyword);
            openPage(searchUrl);
            handleVerificationIfNeeded();
            Document document = getPageSourceAsDocument();
            System.out.println("Keresés kulcsszóval: " + keyword);
            return parseListings(document);
        } catch (Exception e) {
            throw new RuntimeException("Scraping failed for marketplace: " + getMarketplace(), e);
        }
    }

    protected void openPage(String url) {
        driver.get(url);
    }

    protected void initDriver() {
        if (driver == null) {
            driver = SeleniumDriverFactory.createChromeDriver();
            wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        }
    }

    protected void handleVerificationIfNeeded() {
        WebElement checkbox = findVerificationCheckbox();
        if (checkbox != null) {
            handleHumanVerification(checkbox);
            waitForVerificationCompletion(checkbox);
        }
    }

    protected WebElement findVerificationCheckbox() {
        return null;
    }

    protected void handleHumanVerification(WebElement checkbox) {
    }

    protected void waitForVerificationCompletion(WebElement checkbox) {
    }

    protected Document getPageSourceAsDocument() {
        waitForPageToLoad();
        String pageSource = driver.getPageSource();
        assert pageSource != null;
        Document doc = Jsoup.parse(pageSource);
        doc.setBaseUri(Objects.requireNonNull(driver.getCurrentUrl()));
        return doc;
    }

    protected void waitForPageToLoad() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10)).until(webDriver ->
                    ((JavascriptExecutor) webDriver)
                            .executeScript("return document.readyState")
                            .equals("complete")
            );
            Thread.sleep(2000);
        } catch (Exception e) {
            throw new RuntimeException("Page load wait failed", e);
        }
    }

    protected abstract String buildSearchUrl(String keyword);

    protected abstract List<ListingDto> parseListings(Document document);
}
