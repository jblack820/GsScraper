package com.example.GsScraper.repository;

import com.example.GsScraper.model.entity.ListingEntity;
import com.example.GsScraper.model.enumerated.Marketplace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ListingRepository extends JpaRepository<ListingEntity, Long> {

    Optional<ListingEntity> findByUrl(String url);

    Optional<ListingEntity> findByMarketplaceAndUrl(Marketplace marketplace, String url);

    boolean existsByMarketplaceAndUrl(Marketplace marketplace, String url);

    List<ListingEntity> findByMarketplace(Marketplace marketplace);
}
