package com.example.GsScraper.service;

import com.example.GsScraper.model.InstrumentDto;
import org.springframework.beans.factory.annotation.Value;

import java.net.http.HttpRequest;

import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
public class TelegramNotifier {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.chat.id}")
    private String chatId;

    public void sendInstrumentNotification(InstrumentDto instrument) {
        String message = constructInstrumentMessage(instrument);
        sendTelegramMessage(message);
    }

    public void sendSimpleMessage(String message) {
        sendTelegramMessage(message);
    }

    private void sendTelegramMessage(String message) {
        String url = String.format(
                "https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s&parse_mode=HTML",
                botToken, chatId, URLEncoder.encode(message, StandardCharsets.UTF_8)
        );

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
            client.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (Exception e) {
            System.err.println("❌ Telegram értesítés sikertelen: " + e.getMessage());
        }
    }

    private static String constructInstrumentMessage(InstrumentDto instrument) {
        return String.format(
                " \n " +
                "<b>%s</b>\n\n" +
                        "<a href=\"%s\">📌 %s</a> \n" +
                        "<b>📅 </b> %s\n" +
                        "<b>💰 </b> %s\n" +
                        "<a href=\"%s\">🔗 Kép link</a> \n\n",
                instrument.getTitle(),
                instrument.getUrl(),
                instrument.getUrl(),
                instrument.getDate(),
                instrument.getPrice(),
                instrument.getTitlePictureURL()
        );
    }
}

