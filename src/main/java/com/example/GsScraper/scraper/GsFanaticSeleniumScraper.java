package com.example.GsScraper.scraper;

import com.example.GsScraper.exception.HumanVerificationException;

import com.example.GsScraper.model.dto.ListingDto;
import com.example.GsScraper.model.enumerated.Marketplace;
import com.example.GsScraper.builder.GsFanaticSearchUrlBuilder;
import com.example.GsScraper.util.ScraperUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
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
public class GsFanaticSeleniumScraper extends AbstractSeleniumMarketplaceScraper {

    private static final String BASE_URL =
            "https://gsfanatic.com/hu/search?choice=instrument&search_settings=de4ea6de49bf5d49f815e194b744b8f7&q=";

    private final GsFanaticSearchUrlBuilder searchUrlBuilder;

    public GsFanaticSeleniumScraper(GsFanaticSearchUrlBuilder searchUrlBuilder) {
        this.searchUrlBuilder = searchUrlBuilder;
    }

    @Override
    public Marketplace getMarketplace() {
        return Marketplace.GSFANATIC;
    }

    @Override
    protected String buildSearchUrl(String keyword) {
        return searchUrlBuilder.buildSearchUrl(BASE_URL, keyword);
    }

    @Override
    protected List<ListingDto> parseListings(Document doc) {
        List<ListingDto> listings = new ArrayList<>();
        Elements cards = doc.select("div.card");

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
                String price = priceEl.text().trim();
                String imageUrl = imgEl.absUrl("src");
                String url = urlEl.absUrl("href");

                listings.add(new ListingDto(
                        url,
                        LocalDate.now(),
                        title,
                        price,
                        imageUrl
                ));
            } catch (Exception e) {
                System.err.println("GSFanatic card parse failed: " + e.getMessage());
            }
        }

        System.out.println("GSFanatic találatok: " + listings.size());
        return listings;
    }

    @Override
    protected WebElement findVerificationCheckbox() {
        try {
            System.out.println("  -- WebDRIVER: Searching for human verification checkbox...");

            Thread.sleep(2500);

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

                        if (checkbox.isDisplayed()
                                && "checkbox".equals(checkbox.getAttribute("type"))
                                && checkbox.getAttribute("id") != null) {
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

    @Override
    protected void handleHumanVerification(WebElement checkbox) {
        try {
            ScraperUtils.waitRandomMillis(1000, 3000);
            simulateMouseMovements();
            clickCheckbox(checkbox);

            wait.until(ExpectedConditions.invisibilityOfElementLocated(
                    By.cssSelector("input.form-check-input[type='checkbox']")
            ));

            Thread.sleep(ScraperUtils.getRandomIntBetween(1, 3));

        } catch (Exception e) {
            try {
                throw new HumanVerificationException(
                        "-- WebDRIVER: Error during handling human verification: " + e.getMessage()
                );
            } catch (HumanVerificationException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @Override
    protected void waitForVerificationCompletion(WebElement checkbox) {
        try {
            System.out.println("  -- WebDRIVER: Waiting after checkbox click...");

            WebDriverWait localWait = new WebDriverWait(driver, Duration.ofSeconds(15));
            localWait.until(ExpectedConditions.or(
                    ExpectedConditions.invisibilityOf(checkbox),
                    ExpectedConditions.stalenessOf(checkbox),
                    ExpectedConditions.presenceOfElementLocated(
                            By.cssSelector("div.card, .product-item, .ad-item")
                    )
            ));
        } catch (Exception e) {
            System.err.println("  -- WebDRIVER: Verification wait error: " + e.getMessage());
        }
    }

    private void clickCheckbox(WebElement checkbox) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", checkbox);
        System.out.println("  -- WebDRIVER: Checkbox clicked");
    }

    private void simulateMouseMovements() {
        try {
            System.out.println("  -- WebDRIVER: Handling Human verification...");
            Actions actions = new Actions(driver);
            Random random = new Random();

            actions.moveToElement(driver.findElement(By.tagName("body")), 0, 0).perform();
            ScraperUtils.waitRandomMillis(500, 1500);

            for (int i = 0; i < random.nextInt(3) + 3; i++) {
                int xOffset = random.nextInt(16) - 8;
                int yOffset = random.nextInt(16) - 8;
                actions.moveByOffset(xOffset, yOffset).perform();
                ScraperUtils.waitRandomMillis(250, 600);
            }

            System.out.println("  -- WebDRIVER: Mouse simulation completed");

        } catch (Exception e) {
            System.err.println("  -- WebDRIVER: Mouse simulation error: " + e.getMessage());
        }
    }
}