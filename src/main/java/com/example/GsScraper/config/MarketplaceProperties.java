package com.example.GsScraper.config;

import java.time.LocalTime;
import java.util.Objects;

public class MarketplaceProperties {

    private boolean onlyDaytime;
    private LocalTime startTime;
    private LocalTime endTime;
    private int interFetchSeconds;
    private boolean summaryReports;

    public boolean isOnlyDaytime() {
        return onlyDaytime;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public int getInterFetchSeconds() {
        return interFetchSeconds;
    }

    public boolean isSummaryReports() {
        return summaryReports;
    }

    public void setOnlyDaytime(boolean onlyDaytime) {
        this.onlyDaytime = onlyDaytime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public void setInterFetchSeconds(int interFetchSeconds) {
        this.interFetchSeconds = interFetchSeconds;
    }

    public void setSummaryReports(boolean summaryReports) {
        this.summaryReports = summaryReports;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MarketplaceProperties that = (MarketplaceProperties) o;
        return onlyDaytime == that.onlyDaytime && interFetchSeconds == that.interFetchSeconds && summaryReports == that.summaryReports && Objects.equals(startTime, that.startTime) && Objects.equals(endTime, that.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(onlyDaytime, startTime, endTime, interFetchSeconds, summaryReports);
    }

    @Override
    public String toString() {
        return "MarketplaceProperties{" +
                "onlyDaytime=" + onlyDaytime +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", interFetchSeconds=" + interFetchSeconds +
                ", summaryReports=" + summaryReports +
                '}';
    }
}
