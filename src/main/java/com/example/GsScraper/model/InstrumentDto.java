package com.example.GsScraper.model;

import java.time.LocalDate;

public class InstrumentDto {
    private String url;
    private LocalDate date;
    private String title;
    private String price;
    private String titlePictureURL;
    private boolean active;

    public InstrumentDto(String url, LocalDate date, String title, String price, String titlePictureURL, boolean active) {
        this.url = url;
        this.date = date;
        this.title = title;
        this.price = price;
        this.titlePictureURL = titlePictureURL;
        this.active = active;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getTitlePictureURL() {
        return titlePictureURL;
    }

    public void setTitlePictureURL(String titlePictureURL) {
        this.titlePictureURL = titlePictureURL;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
