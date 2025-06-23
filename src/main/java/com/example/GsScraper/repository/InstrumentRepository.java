package com.example.GsScraper.repository;

import com.example.GsScraper.model.InstrumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstrumentRepository extends JpaRepository<InstrumentEntity, Long> {
    boolean existsByTitlePictureURL(String titlePictureURL);
}
