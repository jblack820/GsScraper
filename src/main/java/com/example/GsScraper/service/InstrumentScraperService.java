package com.example.GsScraper.service;

import com.example.GsScraper.model.InstrumentEntity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class InstrumentScraperService {

    private static final String GS_FANATIC_BASE_SEARCH_URL = "https://gsfanatic.com/hu/search?choice=instrument&search_settings=de4ea6de49bf5d49f815e194b744b8f7&q=";

    public List<InstrumentEntity> fetchInstruments(String keyword) throws IOException {
        Document doc = fetchItems(GS_FANATIC_BASE_SEARCH_URL + keyword);
        Elements cards = doc.select("div.card");
        return getInstrumentsFromCards(cards);
    }

    private static Document fetchItems(String searchUrl) throws IOException {
        return Jsoup.connect(searchUrl)
                .headers(constructHeaderForGsFanatic())
                .timeout(10_000)
                .get();
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

    private static Map<String, String> constructHeaderForGsFanatic() {
        return Map.of(
                "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36",
                "Accept-Language", "en-US,en;q=0.9",
                "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
                "Connection", "keep-alive",
                "Referer", "https://gsfanatic.com/"
        );
    }
}

