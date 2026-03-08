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

    private final Map<String, Subscription> subscriptions = new ConcurrentHashMap<>();
    private final SimpMessagingTemplate messagingTemplate;
    static final Logger log =
            LoggerFactory.getLogger(PlayersWsController.class);


    @MessageMapping("/subscribe/players")
    public void subscribe(Subscription sub, Principal principal, StompHeaderAccessor accessor) {
        CustomUserDetails ud = (CustomUserDetails) ((Authentication) principal).getPrincipal();
        String sessionId = accessor.getSessionId();
        sub.setUserId(ud.getId());
        subscriptions.put(sessionId, sub);
        log.info("Get subscribe to PlayerWS {} | {}", sessionId, sub);
    }

    @MessageMapping("/move")
    public void movePlayer(LocationPlayerDto locationPlayerDto, Principal principal) {
        CustomUserDetails ud = (CustomUserDetails) ((Authentication) principal).getPrincipal();
        locationPlayerDto.setUserId(ud.getId());
        locationPlayerDto.setUsername(ud.getUsername());

        messagingTemplate.convertAndSend(
                "/topic/all_players",
                locationPlayerDto
        );
        log.info("SEND TO ALL PLAYERS lat={} lon={}",locationPlayerDto.getLat(), locationPlayerDto.getLon());
    }
}
