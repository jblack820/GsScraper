package com.example.GsScraper.model.dto;

import java.time.LocalDate;

public class ListingDto {

    private String url;
    private LocalDate date;
    private String title;
    private String price;
    private String titlePictureUrl;

    public ListingDto(String url, LocalDate date, String title, String price, String titlePictureUrl) {
        this.url = url;
        this.date = date;
        this.title = title;
        this.price = price;
        this.titlePictureUrl = titlePictureUrl;
    }

    public ListingDto() {}

    public String getUrl() {
        return url;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getTitle() {
        return title;
    }

    public String getPrice() {
        return price;
    }

    public String getTitlePictureUrl() {
        return titlePictureUrl;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setTitlePictureUrl(String titlePictureUrl) {
        this.titlePictureUrl = titlePictureUrl;
    }
}