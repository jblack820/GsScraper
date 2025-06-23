package com.example.GsScraper.model;

import jakarta.persistence.Column;

import java.time.LocalDate;

public class InstrumentDto {
    private String url;
    private LocalDate date;
    private String title;
    private String price;
    private String titlePictureURL;

    public InstrumentDto(String url, LocalDate date, String title, String price, String titlePictureURL) {
        this.url = url;
        this.date = date;
        this.title = title;
        this.price = price;
        this.titlePictureURL = titlePictureURL;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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

    @Override
    public String toString() {
        return "InstrumentDto{" +
                "date=" + date +
                ", url='" + url + '\'' +
                ", title='" + title + '\'' +
                ", price='" + price + '\'' +
                ", titlePictureURL='" + titlePictureURL + '\'' +
                '}';
    }
}
