package com.kyas.wolkandhold.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@AllArgsConstructor
public class LocationPlayerDto {
    @Getter
    @Setter
    private Long userId;
    @Getter
    @Setter
    private String username;

    @Getter
    @Setter
    private double lat;

    @Getter
    @Setter
    private double lon;

    @Getter
    @Setter
    @JsonProperty("isCapture")
    private boolean isCapture;
}
