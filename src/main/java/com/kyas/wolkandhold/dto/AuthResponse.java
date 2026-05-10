package com.kyas.wolkandhold.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class AuthResponse {

    @Getter
    @Setter
    private String token;

    @Getter
    @Setter
    private Long userId;

    @Getter
    @Setter
    private String username;


    public AuthResponse() {

    }

}
