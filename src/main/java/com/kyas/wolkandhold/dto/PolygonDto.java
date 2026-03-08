package com.kyas.wolkandhold.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@ToString
public class PolygonDto {

    @Setter
    @Getter
    @Min(message = "UserId is required", value = 1)
    private UserDto owner;
    @NotBlank(message = "Points is required")
    @Setter
    @Getter
    private List<PointDto> points;
    @Setter
    @Getter
    private double area_m2;
    @Setter
    @Getter
    @Min(message = "Last update timestamp is required", value = 1)
    private long lastUpdated;

    @Setter
    @Getter
    private String title;

    public PolygonDto() {
    }


}
