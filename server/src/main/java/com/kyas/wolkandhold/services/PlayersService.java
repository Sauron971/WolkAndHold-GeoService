package com.kyas.wolkandhold.services;

import com.kyas.wolkandhold.dao.PolygonRepository;
import com.kyas.wolkandhold.dao.UserRepository;
import com.kyas.wolkandhold.projections.LeaderProjection;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlayersService {

    private final PolygonRepository polygonRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;
    static final Logger log =
            LoggerFactory.getLogger(PlayersService.class);

    public List<LeaderProjection> getLeaderboard() {

        return polygonRepository.getLeaderboard();
    }
}
