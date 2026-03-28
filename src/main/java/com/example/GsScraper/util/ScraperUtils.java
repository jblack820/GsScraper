package com.example.GsScraper.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

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

    public static void waitRandomMillis(double minMillis, double maxMillis) {
        if (minMillis > maxMillis) {
            throw new IllegalArgumentException("minMillis nem lehet nagyobb, mint maxMillis");
        }

        double randomMillis = minMillis + Math.random() * (maxMillis - minMillis);

        try {
            Thread.sleep((long) randomMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void waitRandomMillisWithConsoleMessage(double minMillis, double maxMillis) {
        if (minMillis > maxMillis) {
            throw new IllegalArgumentException("minMillis nem lehet nagyobb, mint maxMillis");
        }

        double randomMillis = minMillis + Math.random() * (maxMillis - minMillis);

        try {
            System.out.println("Várakozás: " + randomMillis + " milliszekundum");
            Thread.sleep((long) randomMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static int getRandomIntBetween(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}
