package com.kyas.wolkandhold.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
public class Subscription {
    private Long userId;
    private double lat;
    private double lon;
    private double radius;


}
