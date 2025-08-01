package com.kyas.wolkandhold;


import com.yandex.mapkit.geometry.Point;

import java.util.LinkedList;
import java.util.List;

public class BufferedRoute {
    private static final List<Point> points = new LinkedList<>();
    public static void add(Point point) {
        points.add(point);
    }
    public static List<Point> getAll() {
        return new LinkedList<>(points);
    }
    public static void clear() {
        points.clear();
    }
}
