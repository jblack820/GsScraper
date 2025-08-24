package com.example.GsScraper.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ScraperUtils {

    public static String getFormattedCurrentDateTime() {
        LocalDateTime ldt = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy - hh:mm");
        String dateTime = ldt.format(formatter);
        return dateTime;
    }

    public static void waitForSeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void waitForSecondsWithConsoleMessage(int seconds, String message) {
        try {
            System.out.println(message);
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
