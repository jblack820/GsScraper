package com.example.GsScraper.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.LocalTime;

@ConfigurationProperties(prefix = "gs")
public class GsFanaticProperties {

    private boolean scrapingOnlyDaytime;
    private LocalTime serviceStartTime;
    private LocalTime serviceEndTime;
    private int interFetchSeconds;
    private boolean summaryReports;

    public boolean isOnlyDaytime() {
        return scrapingOnlyDaytime;
    }

    public void setScrapingOnlyDaytime(boolean scrapingOnlyDaytime) {
        this.scrapingOnlyDaytime = scrapingOnlyDaytime;
    }

    public LocalTime getStartTime() {
        return serviceStartTime;
    }

    public void setServiceStartTime(LocalTime serviceStartTime) {
        this.serviceStartTime = serviceStartTime;
    }

    public LocalTime getEndTime() {
        return serviceEndTime;
    }

    public void setServiceEndTime(LocalTime serviceEndTime) {
        this.serviceEndTime = serviceEndTime;
    }

    public int getInterFetchSeconds() {
        return interFetchSeconds;
    }

    public void setInterFetchSeconds(int interFetchSeconds) {
        this.interFetchSeconds = interFetchSeconds;
    }

    public boolean isSummaryReports() {
        return summaryReports;
    }

    public void setSummaryReports(boolean summaryReports) {
        this.summaryReports = summaryReports;
    }
}
