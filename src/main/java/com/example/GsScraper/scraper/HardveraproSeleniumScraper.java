package com.example.GsScraper.scraper;

import com.example.GsScraper.model.dto.ListingDto;
import com.example.GsScraper.model.enumerated.Marketplace;
import com.example.GsScraper.builder.HardveraproSearchUrlBuilder;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class HardveraproSeleniumScraper extends AbstractSeleniumMarketplaceScraper {

    private static final String BASE_URL =
            "https://hardverapro.hu/aprok/hardver/merevlemez_ssd/ssd/m2_nvme/2_tb/";

    private final HardveraproSearchUrlBuilder searchUrlBuilder;

    public HardveraproSeleniumScraper(HardveraproSearchUrlBuilder searchUrlBuilder) {
        this.searchUrlBuilder = searchUrlBuilder;
    }

    @Override
    public Marketplace getMarketplace() {
        return Marketplace.HARDVERAPRO;
    }

    @Override
    protected String buildSearchUrl(String keyword) {
        return searchUrlBuilder.buildSearchUrl(BASE_URL, keyword);
    }

    @Override
    protected List<ListingDto> parseListings(Document doc) {
        List<ListingDto> results = new ArrayList<>();
        Elements items = doc.select("div.uad");

        for (Element item : items) {
            try {
                Element titleEl = item.selectFirst("a.uad-title");
                Element priceEl = item.selectFirst(".uad-price");
                Element imgEl = item.selectFirst("img");

                if (titleEl == null) {
                    continue;
                }

                String title = titleEl.text().trim();
                String url = titleEl.absUrl("href");
                String price = priceEl != null ? priceEl.text().trim() : "";
                String img = imgEl != null ? imgEl.absUrl("src") : "";

                results.add(new ListingDto(
                        url,
                        LocalDate.now(),
                        title,
                        price,
                        img
                ));
            } catch (Exception e) {
                System.err.println("Hardverapro parse error: " + e.getMessage());
            }
        }

        System.out.println("Hardverapro találatok: " + results.size());
        return results;
    }
}