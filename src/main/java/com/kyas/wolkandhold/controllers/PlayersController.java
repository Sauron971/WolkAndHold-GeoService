package com.kyas.wolkandhold.controllers;

import com.kyas.wolkandhold.dao.UserRepository;
import com.kyas.wolkandhold.security.JwtUtils;
import com.kyas.wolkandhold.services.PlayersService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/players")
@AllArgsConstructor
public class PlayersController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final UserDetailsService userDetailsService;
    private final PlayersService playersService;
    static final Logger log =
            LoggerFactory.getLogger(PlayersController.class);

    @GetMapping("/leaderboard")
    public ResponseEntity<?> getLeaderboard() {
        return ResponseEntity.ok(playersService.getLeaderboard());
    }
}
