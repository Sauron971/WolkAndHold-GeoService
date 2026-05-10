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
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
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

    @MessageMapping("/subscribe/polygons")
    public void subscribe(Subscription sub, Principal principal, StompHeaderAccessor accessor) {
        CustomUserDetails ud = (CustomUserDetails) ((Authentication) principal).getPrincipal();
        String username = principal.getName();
        sub.setUserId(ud.getId());
        subscriptions.put(username, sub);
        log.info("Get subscribe to PolygonWS {} | {}", username, sub);
    }

    public void notifyPolygonUpdated(PolygonResponse polygon)  {
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
    public void notifyPolygonUpdated(List<PolygonResponse> polygons)  {
        for (var entry : subscriptions.entrySet()) {
            Subscription sub = entry.getValue();

            log.info("Notify poly update is not send");
            if (polygonInsideRadius(polygons, sub)) {
                messagingTemplate.convertAndSendToUser(
                        entry.getKey(),
                        "/queue/polygons",
                        polygons
                );
                log.info("Notify polygons update: {}", polygons );
            }

        }
    }

    private boolean polygonInsideRadius(PolygonResponse polygon, Subscription sub)  {
        if (polygon.getWkt() == null || polygon.getWkt().isBlank()) {
            // Deletion event (empty geometry) must be delivered to keep clients in sync.
            return true;
        }
        Point center = geometryFactory.createPoint(new Coordinate(sub.getLon(), sub.getLat()));
        Geometry buffer = center.buffer(sub.getRadius() / 111_000.0); 

        Geometry poly = fromWkt(polygon.getWkt());
        return buffer.intersects(poly);
    }
    private boolean polygonInsideRadius(List<PolygonResponse> polygons, Subscription sub)  {
        if (polygons == null || polygons.isEmpty()) {
            return false;
        }
        boolean hasDeletionEvents = polygons.stream()
                .map(PolygonResponse::getWkt)
                .anyMatch(wkt -> wkt == null || wkt.isBlank());
        Point center = geometryFactory.createPoint(new Coordinate(sub.getLon(), sub.getLat()));
        Geometry buffer = center.buffer(sub.getRadius() / 111_000.0);
        boolean isInside = false;
        for (PolygonResponse polygon : polygons) {
            if (polygon.getWkt() == null || polygon.getWkt().isBlank()) {
                continue;
            }
            Geometry poly = fromWkt(polygon.getWkt());
            log.info("Geometry from wkt = {}", poly.toString());
            log.info("Geometry wkt = {}", polygon.getWkt());
            isInside = buffer.intersects(poly);
            if (isInside)
                return isInside;
        }
        return hasDeletionEvents;
    }

    private Geometry fromWkt(String wkt) {
        try {
            return new WKTReader(geometryFactory).read(wkt);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid WKT: " + wkt, e);
        }
    }
}
