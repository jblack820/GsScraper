package com.example.GsScraper.model.entity;

import com.example.GsScraper.model.enumerated.Marketplace;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "search_keyword",
        uniqueConstraints = @UniqueConstraint(columnNames = {"keyword", "marketplace"}))
public class SearchKeywordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String keyword;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Marketplace marketplace;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public SearchKeywordEntity() {
        this.createdAt = LocalDateTime.now();
    }

    public SearchKeywordEntity(String keyword, Marketplace marketplace) {
        this.keyword = keyword;
        this.marketplace = marketplace;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Marketplace getMarketplace() {
        return marketplace;
    }

    public void setMarketplace(Marketplace marketplace) {
        this.marketplace = marketplace;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "SearchKeywordEntity{" +
                "id=" + id +
                ", keyword='" + keyword + '\'' +
                ", marketplace=" + marketplace +
                '}';
    }
}