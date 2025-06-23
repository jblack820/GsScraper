package com.example.GsScraper.model;

public class InstrumentMapper {

    public static InstrumentDto toDto(InstrumentEntity instrument) {
        return new InstrumentDto(
                instrument.getUrl(),
                instrument.getDate(),
                instrument.getTitle(),
                instrument.getPrice(),
                instrument.getTitlePictureURL());
    }

    public static InstrumentEntity toEntity(InstrumentDto instrument) {
        return new InstrumentEntity(
                instrument.getUrl(),
                instrument.getDate(),
                instrument.getTitle(),
                instrument.getPrice(),
                instrument.getTitlePictureURL());
    }
}
