package com.kyas.wolkandhold.security;

import com.kyas.wolkandhold.controllers.AuthController;
import com.kyas.wolkandhold.services.PostUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthChannelInterceptor implements ChannelInterceptor {

    private final JwtUtils jwtUtils;
    private final PostUserDetailsService userDetailsService;
    static final Logger log =
            LoggerFactory.getLogger(AuthChannelInterceptor.class);


    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String jwt = authHeader.substring(7);
                if (jwtUtils.validate(jwt)) {
                    String username = jwtUtils.getUsername(jwt);
                    CustomUserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    Authentication authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                    accessor.setUser(authentication);
                } else {
                    throw new IllegalArgumentException("Invalid JWT token");
                }
            } else {
                throw new IllegalArgumentException("Authorization header missing");
            }
        }

        return message;
    }
}
