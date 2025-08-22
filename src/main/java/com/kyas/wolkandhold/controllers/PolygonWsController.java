package com.kyas.wolkandhold.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyas.wolkandhold.dto.PointDto;
import com.kyas.wolkandhold.dto.PolygonResponse;
import com.kyas.wolkandhold.dto.Subscription;
import com.kyas.wolkandhold.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@RequiredArgsConstructor
public class PolygonWsController {

    private final SimpMessagingTemplate messagingTemplate;
    private final Map<String, Subscription> subscriptions = new ConcurrentHashMap<>();
    static final Logger log =
            LoggerFactory.getLogger(PolygonWsController.class);

    private final GeometryFactory geometryFactory = new GeometryFactory();

    @MessageMapping("/subscribe")
    public void subscribe(Subscription sub, Principal principal, StompHeaderAccessor accessor) {
        CustomUserDetails ud = (CustomUserDetails) ((Authentication) principal).getPrincipal();
        String sessionId = accessor.getSessionId();
        sub.setUserId(ud.getId());
        subscriptions.put(sessionId, sub);
        log.info("Getting subscribe {} | {}", sessionId, sub);
    }

    public void notifyPolygonUpdated(PolygonResponse polygon) throws JsonProcessingException {
        for (var entry : subscriptions.entrySet()) {
            Subscription sub = entry.getValue();

            if (polygonInsideRadius(polygon, sub)) {
                messagingTemplate.convertAndSendToUser(
                        entry.getKey(),
                        "/queue/polygons",
                        polygon
                );
                log.info("Notify polygon update: {}", polygon.getId() );
            }

        }
    }

    private boolean polygonInsideRadius(PolygonResponse polygon, Subscription sub) throws JsonProcessingException {
        Point center = geometryFactory.createPoint(new Coordinate(sub.getLon(), sub.getLat()));
        Geometry buffer = center.buffer(sub.getRadius() / 111_000.0); 

        Geometry poly = fromJson(polygon.getWkt());
        return buffer.intersects(poly);
    }

    private Polygon fromJson(String pointsJson) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        List<PointDto> points = objectMapper.readValue(pointsJson, new TypeReference<>() {});


        Coordinate[] coords = points.stream()
                .map(p -> new Coordinate(p.getLongitude(), p.getLatitude()))
                .toArray(Coordinate[]::new);


        if (!coords[0].equals2D(coords[coords.length - 1])) {
            Coordinate[] closed = new Coordinate[coords.length + 1];
            System.arraycopy(coords, 0, closed, 0, coords.length);
            closed[closed.length - 1] = coords[0];
            coords = closed;
        }


        LinearRing shell = geometryFactory.createLinearRing(coords);
        return geometryFactory.createPolygon(shell);
    }
}
