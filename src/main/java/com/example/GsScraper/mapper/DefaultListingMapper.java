package com.example.GsScraper.mapper;

import com.example.GsScraper.model.dto.ListingDto;
import com.example.GsScraper.model.entity.ListingEntity;
import com.example.GsScraper.model.enumerated.Marketplace;
import org.springframework.stereotype.Component;

@Component
public class DefaultListingMapper implements ListingMapper {

    @Override
    public ListingEntity toEntity(ListingDto dto, Marketplace marketplace) {
        ListingEntity entity = new ListingEntity();

        entity.setUrl(dto.getUrl());
        entity.setDate(dto.getDate());
        entity.setTitle(dto.getTitle());
        entity.setPrice(dto.getPrice());
        entity.setTitlePictureUrl(dto.getTitlePictureUrl());
        entity.setMarketplace(marketplace);

        return entity;
    }

    @Override
    public ListingDto toDto(ListingEntity entity) {
        return new ListingDto(
                entity.getUrl(),
                entity.getDate(),
                entity.getTitle(),
                entity.getPrice(),
                entity.getTitlePictureUrl()
        );
    }

}