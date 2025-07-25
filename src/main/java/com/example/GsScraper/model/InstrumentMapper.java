package com.example.GsScraper.model;

import java.util.ArrayList;
import java.util.List;

public class InstrumentMapper {

    public static InstrumentDto toDto(InstrumentEntity instrument) {
        return new InstrumentDto(
                instrument.getUrl(),
                instrument.getDate(),
                instrument.getTitle(),
                instrument.getPrice(),
                instrument.getTitlePictureURL(),
                instrument.isActive());
    }

    public static List<InstrumentDto> toDtos(List<InstrumentEntity> instruments) {
        List<InstrumentDto> instrumentDtos = new ArrayList<InstrumentDto>();
        for (InstrumentEntity instrumentEntity : instruments) {
            instrumentDtos.add(toDto(instrumentEntity));
        }
        return instrumentDtos;
    }

}
