package com.kyas.wolkandhold.data.api.requests;

import com.yandex.mapkit.geometry.Point;

import java.util.List;

public class PolygonRequest {
    private UserRequest owner;
    private List<Point> points;
    private double area_m2;
    private long lastUpdated;

    public PolygonRequest(UserRequest owner, List<Point> points, double area_m2, long lastUpdated) {
        this.owner = owner;
        this.points = points;
        this.area_m2 = area_m2;
        this.lastUpdated = lastUpdated;
    }

    @Override
    public String toString() {
        return "PolygonRequest{" +
                "owner=" + owner +
                ", points=" + points +
                ", area_m2=" + area_m2 +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}
