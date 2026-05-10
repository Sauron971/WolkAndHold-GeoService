package com.kyas.wolkandhold.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kyas.wolkandhold.dao.UserRepository;
import com.kyas.wolkandhold.dto.PolygonDto;
import com.kyas.wolkandhold.dto.PolygonResponse;
import com.kyas.wolkandhold.dto.UserDto;
import com.kyas.wolkandhold.entity.PolygonEntity;
import com.kyas.wolkandhold.security.CustomUserDetails;
import com.kyas.wolkandhold.services.PolygonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/polygons")
public class PolygonController {

    private final PolygonService polygonService;
    static final Logger log =
            LoggerFactory.getLogger(PolygonController.class);

    public PolygonController(PolygonService polygonService) {
        this.polygonService = polygonService;
    }

    @GetMapping("/{lat}/{lon}/{radius}")
    public List<PolygonResponse> getPolygonsInRadius(@PathVariable double lat,
                                                   @PathVariable double lon,
                                                   @PathVariable double radius) {

        log.info("Get request sending polygons in radius lat{}, lon{}, radius{}", lat, lon, radius);
        return polygonService.findInRadius(lat, lon, radius).stream()
                .map((entity) -> {
                    String wkt = entity.getArea().toText();

                    PolygonResponse resp = new PolygonResponse(
                            entity.getId(),
                            new UserDto(entity.getOwner().getId(),
                                    entity.getOwner().getUsername()),
                            entity.getSquare(),
                            entity.getLastUpdated(),
                            wkt,
                            entity.getTitle()
                    );
                    return resp;
                }).toList();
    }

    @PostMapping()
    public ResponseEntity<?> createPolygon(@AuthenticationPrincipal CustomUserDetails ud,
                                           @RequestBody PolygonDto polygonDto) throws JsonProcessingException {
        polygonDto.setOwner(new UserDto(ud.getId(), ud.getUsername()));
        PolygonEntity entity = polygonService.createPolygon(polygonDto);
        String wkt = entity.getArea().toText();

        PolygonResponse resp = new PolygonResponse(
                entity.getId(),
                new UserDto(entity.getOwner().getId(),
                        entity.getOwner().getUsername()),
                entity.getSquare(),
                entity.getLastUpdated(),
                wkt,
                entity.getTitle()
        );
        log.info("Insert polygon userId = {}, dto = {}", ud.getId(), polygonDto );
        return ResponseEntity.ok(resp);
    }

}
