package com.kyas.wolkandhold.controllers;

import com.kyas.wolkandhold.dto.LocationPlayerDto;
import com.kyas.wolkandhold.dto.Subscription;
import com.kyas.wolkandhold.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@RequiredArgsConstructor
public class PlayersWsController {

    private final SimpMessagingTemplate messagingTemplate;
    static final Logger log =
            LoggerFactory.getLogger(PlayersWsController.class);


    @MessageMapping("/move")
    public void movePlayer(LocationPlayerDto locationPlayerDto, Principal principal) {
        CustomUserDetails ud = (CustomUserDetails) ((Authentication) principal).getPrincipal();
        locationPlayerDto.setUserId(ud.getId());
        locationPlayerDto.setUsername(ud.getUsername());

        messagingTemplate.convertAndSend(
                "/topic/all_players",
                locationPlayerDto
        );
        log.info("SEND TO ALL PLAYERS lat={} lon={} isCapturing?={}",locationPlayerDto.getLat(), locationPlayerDto.getLon(), locationPlayerDto.isCapture());
    }
}
