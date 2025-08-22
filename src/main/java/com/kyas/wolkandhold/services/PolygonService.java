package com.kyas.wolkandhold.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kyas.wolkandhold.controllers.PolygonWsController;
import com.kyas.wolkandhold.dao.PolygonRepository;
import com.kyas.wolkandhold.dao.UserRepository;
import com.kyas.wolkandhold.dto.PointDto;
import com.kyas.wolkandhold.dto.PolygonDto;
import com.kyas.wolkandhold.dto.PolygonResponse;
import com.kyas.wolkandhold.entity.PolygonEntity;
import com.kyas.wolkandhold.entity.UserEntity;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PolygonService {

    private final PolygonRepository polygonRepository;
    private final UserRepository userRepository;

    private final EntityManager entityManager;
    private final PolygonWsController polygonWsController;


    public List<PolygonEntity> findInRadius(double lat, double lon, double radiusKm) {
        String sql = """
                Select p.* From polygons p
                Where ST_DWithin(
                    p.area::geography,
                    ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography,
                    :radius)
                """;
        return entityManager.createNativeQuery(sql, PolygonEntity.class)
                .setParameter("lon", lon)
                .setParameter("lat", lat)
                .setParameter("radius", radiusKm*1000)
                .getResultList();
    }

    public PolygonEntity createPolygon(PolygonDto polygonDto) {
        Polygon jtsPolygon = fromPoints(polygonDto.getPoints());

        UserEntity user = userRepository.findById(polygonDto.getOwner().getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        PolygonEntity polygon = new PolygonEntity();
        polygon.setOwner(user);
        polygon.setArea(jtsPolygon);
        polygon.setSquare(polygonDto.getArea_m2());
        polygon.setCreatedAt(System.currentTimeMillis());
        polygon.setLastUpdated(polygonDto.getLastUpdated());

        return polygonRepository.save(polygon);
    }
    @Transactional
    public PolygonEntity updatePolygonForUser(Long userId, PolygonDto polygonDto) {
        PolygonEntity polygon = polygonRepository.findUserById(userId).get();
        if (polygon == null) {
            throw new RuntimeException("Polygon not found");
        }
        Polygon jtsPolygon = fromPoints(polygonDto.getPoints());
        polygon.setArea(jtsPolygon);
        polygon.setSquare(polygonDto.getArea_m2());
        polygon.setLastUpdated(System.currentTimeMillis());

        return polygonRepository.save(polygon);
    }

    @Transactional
    public PolygonEntity upsertPolygon(Long userId, PolygonDto dto) throws JsonProcessingException {
        Polygon jtsPolygon = fromPoints(dto.getPoints());

        //Проверяем находится ли внутри
        boolean isInside = !entityManager.createNativeQuery("""
            SELECT id From polygons
            WHERE user_id != :userId 
                And ST_Within(:poly, area)""")
                .setParameter("userId", userId)
                .setParameter("poly", jtsPolygon)
                .getResultList().isEmpty();
        if (isInside) {
            throw new RuntimeException("Polygon fully inside another territory");
        }
        //обновляем чужие если пересекается
        entityManager.createNativeQuery("""
            UPDATE polygons
            SET area = ST_Difference(area, :poly)
            WHERE user_id != :userId
            AND ST_Intersects(area, :poly)""")
                .setParameter("userId", userId)
                .setParameter("poly", jtsPolygon)
                .executeUpdate();


        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        PolygonEntity entity = polygonRepository.findUserById(userId)
                .orElse(null);
        if (entity != null) {
            entity = entity;
            entity.setArea(jtsPolygon);
            entity.setSquare(caclArea(jtsPolygon));
            entity.setLastUpdated(System.currentTimeMillis());
        } else {
            entity = new PolygonEntity();
            entity.setCreatedAt(System.currentTimeMillis());
            entity.setOwner(user);
            entity.setArea(jtsPolygon);
            entity.setSquare(caclArea(jtsPolygon));
            entity.setLastUpdated(System.currentTimeMillis());
        }

        PolygonEntity saved = polygonRepository.save(entity);
        polygonWsController.notifyPolygonUpdated(PolygonResponse.fromEntity(saved));
        return saved;
    }

    private Polygon fromPoints(List<PointDto> points) {
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        if (points == null || points.size() < 3) {
            throw new IllegalArgumentException("Polygon must have at least 3 points");
        }

        Coordinate[] coords = points.stream()
                .map(p -> new Coordinate(p.longitude, p.latitude))
                .toArray(Coordinate[]::new);

        if (!coords[0].equals2D(coords[coords.length - 1])) {
            Coordinate[] closed = new Coordinate[coords.length + 1];
            System.arraycopy(coords, 0, closed, 0, coords.length);
            closed[closed.length - 1] = coords[0];
            coords = closed;
        }

        LinearRing shell = geometryFactory.createLinearRing(coords);
        return geometryFactory.createPolygon(shell, null);
    }

    private double caclArea(Polygon polygon) {
        return polygon.getArea();
    }

    public void deletePolygon(long id) {
        polygonRepository.deleteById(id);
    }

}
