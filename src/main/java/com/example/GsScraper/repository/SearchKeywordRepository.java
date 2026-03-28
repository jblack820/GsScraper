package com.example.GsScraper.repository;

import com.example.GsScraper.model.entity.SearchKeywordEntity;
import com.example.GsScraper.model.enumerated.Marketplace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SearchKeywordRepository extends JpaRepository<SearchKeywordEntity, Long> {

    List<SearchKeywordEntity> findByMarketplace(Marketplace marketplace);

    Optional<SearchKeywordEntity> findByKeywordAndMarketplace(String keyword, Marketplace marketplace);

    boolean existsByKeywordAndMarketplace(String keyword, Marketplace marketplace);
}
