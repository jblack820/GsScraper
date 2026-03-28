package com.example.GsScraper.mapper;

import com.example.GsScraper.model.dto.ListingDto;
import com.example.GsScraper.model.entity.ListingEntity;
import com.example.GsScraper.model.enumerated.Marketplace;

public interface ListingMapper {
    ListingEntity toEntity(ListingDto dto, Marketplace marketplace);

    ListingDto toDto(ListingEntity entity);
}
