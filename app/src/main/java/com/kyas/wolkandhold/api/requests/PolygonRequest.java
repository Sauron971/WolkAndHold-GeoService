package com.kyas.wolkandhold.api.requests;

import com.yandex.mapkit.geometry.Point;

import java.util.List;

public class PolygonRequest {
    private long userId;
    private List<Point> points;
    private double area_m2;
    private long lastUpdated;
}
