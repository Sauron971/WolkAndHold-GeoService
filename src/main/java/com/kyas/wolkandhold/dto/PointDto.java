package com.kyas.wolkandhold.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class PointDto {
    @Getter
    @Setter
    public double latitude;
    @Getter
    @Setter
    public double longitude;

    public PointDto(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
