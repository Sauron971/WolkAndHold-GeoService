package com.kyas.wolkandhold.data.api.response;

import android.util.Log;

import com.google.gson.Gson;
import com.kyas.wolkandhold.data.database.entities.Polygon;
import com.yandex.mapkit.geometry.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PolygonResponse {
    private long id;
    private UserResponse owner;
    private double square;
    private long lastUpdated;
    private String wkt;

    public List<Polygon> toEntities() {
        String src = wkt == null ? "" : wkt.trim();
        List<Point> points = new ArrayList<>();

        // Парсим, только если строка не пустая
        if (!src.isEmpty()) {
            String body;
            if (src.startsWith("MULTIPOLYGON")) {
                body = src.substring("MULTIPOLYGON".length()).trim();
            } else if (src.startsWith("POLYGON")) {
                body = src.substring("POLYGON".length()).trim();
            } else {
                return Collections.emptyList();
            }

            String flat = body.replace("(", " ").replace(")", " ").trim();
            String[] pairs = flat.split(",");
            for (String pairRaw : pairs) {
                String pair = pairRaw.trim();
                if (pair.isEmpty()) continue;
                String[] parts = pair.split("\\s+");
                if (parts.length < 2) continue;
                try {
                    double lon = Double.parseDouble(parts[0].trim());
                    double lat = Double.parseDouble(parts[1].trim());
                    points.add(new Point(lat, lon));
                } catch (NumberFormatException ignore) {
                    Log.e("PolygonResponse", "Response not parse");
                }
            }

            if (points.size() > 1 && isSame(points.get(0), points.get(points.size() - 1))) {
                points.remove(points.size() - 1);
            }
        }

        // ВАЖНО: Мы создаем объект даже если points пустой!
        Polygon poly = new Polygon();
        poly.id = this.id;
        poly.userId = this.owner.getUserId();
        poly.ownerName = this.owner.getUsername();
        poly.area = this.square;
        poly.lastUpdated = this.lastUpdated;
        // Если points пустой, Gson создаст строку "[]"
        poly.pointsJson = new Gson().toJson(points);

        return List.of(poly);
    }
    // с эпсилоном, чтобы не страдать от погрешностей double
    private boolean isSame(Point a, Point b) {
        double eps = 1e-9;
        return Math.abs(a.getLatitude() - b.getLatitude()) < eps
                && Math.abs(a.getLongitude() - b.getLongitude()) < eps;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public UserResponse getOwner() {
        return owner;
    }

    public void setOwner(UserResponse owner) {
        this.owner = owner;
    }

    public double getSquare() {
        return square;
    }

    public void setSquare(double square) {
        this.square = square;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getWkt() {
        return wkt;
    }

    public void setWkt(String wkt) {
        this.wkt = wkt;
    }

    @Override
    public String toString() {
        return "PolygonResponse{" +
                "id=" + id +
                ", userId=" + owner +
                ", square=" + square +
                ", lastUpdated=" + lastUpdated +
                ", wkt='" + wkt + '\'' +
                '}';
    }
}
