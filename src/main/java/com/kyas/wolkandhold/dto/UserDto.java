package com.kyas.wolkandhold.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class UserDto {
    @Getter
    @Setter
    private long id;

    @Getter
    @Setter
    private String username;

}
