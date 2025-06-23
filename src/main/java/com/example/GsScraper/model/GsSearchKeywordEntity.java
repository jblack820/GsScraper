package com.example.GsScraper.model;

import jakarta.persistence.*;

@Entity
@Table(name = "gs_search_keyword")
public class GsSearchKeywordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String keyword;

    public GsSearchKeywordEntity(Long id, String keyword) {
        this.id = id;
        this.keyword = keyword;
    }

    public GsSearchKeywordEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    @Override
    public String toString() {
        return "GsSearchKeywordEntity{" +
                "id=" + id +
                ", keyword='" + keyword + '\'' +
                '}';
    }
}
