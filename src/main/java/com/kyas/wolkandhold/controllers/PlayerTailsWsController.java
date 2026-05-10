package com.kyas.wolkandhold.controllers;

import com.kyas.wolkandhold.dto.PathDto;
import com.kyas.wolkandhold.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class PlayerTailsWsController {

    private final SimpMessagingTemplate messagingTemplate;
    static final Logger log =
            LoggerFactory.getLogger(PlayerTailsWsController.class);

    @MessageMapping("/cutTail")
    public void tailsCut(@RequestBody PathDto dto, @AuthenticationPrincipal Principal principal) {
        CustomUserDetails ud = (CustomUserDetails) ((Authentication) principal).getPrincipal();
        dto.setUserId(ud.getId());
        dto.setUsername(ud.getUsername());

        messagingTemplate.convertAndSend(
                "/topic/tails",
                dto
        );
        log.info("Cut tail of player (id:{}; name:{}) tail:{}", dto.getUserId(), dto.getUsername(), dto.getPath());
    }
}
