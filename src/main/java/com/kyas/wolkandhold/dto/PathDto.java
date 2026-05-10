package com.kyas.wolkandhold.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@ToString
@RequiredArgsConstructor
public class PathDto {

    @Getter
    @Setter
    private Long userId;

    @Getter
    @Setter
    private String username;

    @Getter
    @Setter
    private List<PointDto> path;


}
