package com.kyas.wolkandhold.data.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.kyas.wolkandhold.data.database.entities.RoutePoint;

import java.util.List;

@Dao
public interface RoutePointDao {
    @Insert
    void insert(RoutePoint point);
    @Insert
    void insertAll(List<RoutePoint> pointList);
    @Query("SELECT * FROM route_points WHERE routeId = :routeId")
    List<RoutePoint> getPointsForRoute(long routeId);

    @Query("SELECT * FROM route_points ")
    List<RoutePoint> getAllPointsRoute();

    // CHANGE: Для корректного удаления маршрута удаляем его точки каскадно
    @Query("DELETE FROM route_points WHERE routeId = :routeId")
    void deleteByRouteId(long routeId);

}
