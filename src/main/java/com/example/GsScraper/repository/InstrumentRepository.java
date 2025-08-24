package com.example.GsScraper.repository;

import com.example.GsScraper.model.InstrumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InstrumentRepository extends JpaRepository<InstrumentEntity, Long> {
    List<InstrumentEntity> findByActiveTrue();
}
