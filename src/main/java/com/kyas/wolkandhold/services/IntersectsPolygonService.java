package com.kyas.wolkandhold.services;


import com.kyas.wolkandhold.dao.PolygonRepository;
import com.kyas.wolkandhold.entity.PolygonEntity;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.union.UnaryUnionOp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IntersectsPolygonService {


    private final EntityManager entityManager;
    private final PolygonRepository polygonRepository;

    @Transactional
    public List<PolygonEntity> mergeOwnPolygonsUpdate(Polygon jtsPolygon, Long userId) {
        List<PolygonEntity> polygons = entityManager.createNativeQuery("""
            SELECT * FROM polygons
            WHERE user_id = :userId
            AND ST_Intersects(area, :poly)
            ORDER BY created_at ASC""", PolygonEntity.class)
                .setParameter("userId", userId)
                .setParameter("poly", jtsPolygon)
                .getResultList();
        List<PolygonEntity> result = new ArrayList<>();
        if (!polygons.isEmpty()) {
            List<Geometry> geometries = new ArrayList<>();
            geometries.add(jtsPolygon.buffer(0));
            for (PolygonEntity poly : polygons) {
                geometries.add(poly.getArea().buffer(0));
            }
            Geometry finalGeometry = UnaryUnionOp.union(geometries).buffer(0);

            entityManager.createNativeQuery("""
            DELETE FROM polygons
            WHERE user_id = :userId
            AND ST_Intersects(area, :poly)""")
                    .setParameter("userId", userId)
                    .setParameter("poly", jtsPolygon)
                    .executeUpdate();
            entityManager.clear();
            PolygonEntity poly = polygons.get(0);
            List<Polygon> polygonParts = extractPolygonParts(finalGeometry);
            for (Polygon part : polygonParts) {
                PolygonEntity newMergedPolygon = new PolygonEntity();
                newMergedPolygon.setOwner(poly.getOwner());
                newMergedPolygon.setArea(part);
                newMergedPolygon.setSquare(0);
                newMergedPolygon.setCreatedAt(poly.getCreatedAt());
                newMergedPolygon.setLastUpdated(System.currentTimeMillis());
                PolygonEntity saved = polygonRepository.save(newMergedPolygon);
                polygonRepository.flush();
                polygonRepository.updateSquareInMeters(saved.getId());
                entityManager.refresh(saved);
                result.add(saved);
            }
            result.addAll(polygons.stream().peek(p -> p.setArea(null)).toList());
        }
        return result;
    }

    @Transactional
    public List<PolygonEntity> updateForeignPolygons(Polygon jtsPolygon, Long userId) {
        List<PolygonEntity> victims = entityManager.createNativeQuery("""
            SELECT * From polygons
            WHERE user_id != :userId
            AND ST_Intersects(area, :poly)""", PolygonEntity.class)
                .setParameter("userId", userId)
                .setParameter("poly", jtsPolygon)
                .getResultList();
        List<PolygonEntity> result = new ArrayList<>();
        Geometry jtsPolyBuffer = jtsPolygon.buffer(0);
        if (!victims.isEmpty()) {
            for (PolygonEntity victim : victims) {
                Geometry victimBuffer = victim.getArea().buffer(0);
                Geometry finalGeom = victimBuffer.difference(jtsPolyBuffer).buffer(0);
                if (finalGeom.isEmpty()) {
                    polygonRepository.delete(victim);
                    victim.setArea(null);
                    result.add(victim);
                    continue;
                }
                List<Polygon> polygonParts = extractPolygonParts(finalGeom);
                if (polygonParts.isEmpty()) {
                    polygonRepository.delete(victim);
                    victim.setArea(null);
                    result.add(victim);
                    continue;
                }
                if (polygonParts.size() == 1) {
                    victim.setArea(polygonParts.get(0));
                    victim.setLastUpdated(System.currentTimeMillis());
                    victim.setSquare(0);
                    polygonRepository.save(victim);
                    polygonRepository.flush();
                    polygonRepository.updateSquareInMeters(victim.getId());
                    entityManager.refresh(victim);
                    result.add(victim);
                    continue;
                }
                polygonRepository.delete(victim);
                victim.setArea(null);
                result.add(victim);

                for (Polygon part : polygonParts) {
                    PolygonEntity partEntity = new PolygonEntity();
                    partEntity.setOwner(victim.getOwner());
                    partEntity.setArea(part);
                    partEntity.setSquare(0);
                    partEntity.setCreatedAt(victim.getCreatedAt());
                    partEntity.setLastUpdated(System.currentTimeMillis());
                    PolygonEntity savedPart = polygonRepository.save(partEntity);
                    polygonRepository.flush();
                    polygonRepository.updateSquareInMeters(savedPart.getId());
                    entityManager.refresh(savedPart);
                    result.add(savedPart);
                }
            }
        }
        return result;
    }

    private List<Polygon> extractPolygonParts(Geometry geometry) {
        List<Polygon> result = new ArrayList<>();
        if (geometry == null || geometry.isEmpty()) {
            return result;
        }
        if (geometry instanceof Polygon polygon) {
            result.add((Polygon) polygon.buffer(0));
            return result;
        }
        for (int i = 0; i < geometry.getNumGeometries(); i++) {
            Geometry part = geometry.getGeometryN(i);
            result.addAll(extractPolygonParts(part));
        }
        return result;
    }

}
