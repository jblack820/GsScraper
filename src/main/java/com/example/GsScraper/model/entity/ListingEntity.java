package com.example.GsScraper.model.entity;

import com.example.GsScraper.model.enumerated.Marketplace;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "listings")
public class ListingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String url;

    private LocalDate date;

    private String title;

    private String price;

    @Column(name = "title_picture_url")
    private String titlePictureUrl;

    private boolean active;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Marketplace marketplace;

    public ListingEntity() {
    }

    public ListingEntity(String url,
                         LocalDate date,
                         String title,
                         String price,
                         String titlePictureUrl,
                         boolean active,
                         Marketplace marketplace) {
        this.url = url;
        this.date = date;
        this.title = title;
        this.price = price;
        this.titlePictureUrl = titlePictureUrl;
        this.active = active;
        this.marketplace = marketplace;
    }

    public Long getId() {
        return id;
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

    public String getTitlePictureUrl() {
        return titlePictureUrl;
    }

    public void setTitlePictureUrl(String titlePictureUrl) {
        this.titlePictureUrl = titlePictureUrl;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Marketplace getMarketplace() {
        return marketplace;
    }

    public void setMarketplace(Marketplace marketplace) {
        this.marketplace = marketplace;
    }

    @Override
    public String toString() {
        return "\nListingEntity{" +
                "url='" + url + '\'' +
                ", marketplace=" + marketplace +
                '}';
    }
}
