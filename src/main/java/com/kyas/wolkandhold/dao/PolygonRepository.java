package com.kyas.wolkandhold.dao;

import com.kyas.wolkandhold.entity.PolygonEntity;
import com.kyas.wolkandhold.projections.LeaderProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PolygonRepository extends JpaRepository<PolygonEntity, Long> {
    Optional<PolygonEntity> findUserById(long userId);
    @Modifying
    @Query(value = "UPDATE polygons SET square = ST_Area(area::geography) WHERE id = :id", nativeQuery = true)
    void updateSquareInMeters(@Param("id") Long id);

    @Query(value = """
            SELECT\s
                u.id,
                u.username,\s
                SUM(square) as totalSquare
                FROM polygons as p
                JOIN users as u ON u.id = p.user_id
                GROUP BY u.username, u.id
                ORDER BY SUM(square) DESC;""", nativeQuery = true)
    List<LeaderProjection> getLeaderboard();

}
