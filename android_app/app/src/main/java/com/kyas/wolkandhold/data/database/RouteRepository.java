package com.kyas.wolkandhold.data.database;

import com.kyas.wolkandhold.data.database.dao.RouteDao;
import com.kyas.wolkandhold.data.database.dao.RoutePointDao;
import com.kyas.wolkandhold.data.database.entities.Route;
import com.kyas.wolkandhold.data.database.entities.RoutePoint;
import com.kyas.wolkandhold.data.Constants;
import com.yandex.mapkit.geometry.Point;

import java.util.ArrayList;
import java.util.List;

public class RouteRepository {
    private final RouteDao routeDao;
    private final RoutePointDao pointDao;
    // ID текущего активного маршрута, INVALID_ROUTE_ID означает отсутствие активного маршрута
    public long currentRouteId = Constants.INVALID_ROUTE_ID;

    public RouteRepository(RouteDao routeDao, RoutePointDao pointDao) {

        this.routeDao = routeDao;
        this.pointDao = pointDao;
    }

    public void addNewRoute(String name, double distance, long userId) {
        Route r = new Route();
        r.name = name;
        r.createdAt = System.currentTimeMillis();
        r.distance = distance;
        r.userId = userId;
        currentRouteId = routeDao.insert(r);
    }
    public void addPointsToRoute(List<Point> pointList) {
        // Проверяем, что у нас есть активный маршрут
        if (currentRouteId == Constants.INVALID_ROUTE_ID)
            return;
        List<RoutePoint> points = new ArrayList<>();
        pointList.forEach(p -> {
            RoutePoint point = new RoutePoint();
            point.routeId = currentRouteId;
            point.latitude = p.getLatitude();
            point.longitude = p.getLongitude();
            points.add(point);
        });
        pointDao.insertAll(points);

    }

    // CHANGE: Обновление маршрута (например, имени)
    public void updateRoute(Route route) {
        routeDao.update(route);
    }

    // CHANGE: Корректное удаление маршрута вместе с его точками
    public void deleteRoute(long routeId) {
        pointDao.deleteByRouteId(routeId);
        routeDao.deleteById(routeId);
    }

}
