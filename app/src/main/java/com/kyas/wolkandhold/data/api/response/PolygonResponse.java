package com.kyas.wolkandhold.data.api.response;

import com.kyas.wolkandhold.data.database.entities.Polygon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PolygonResponse {
    private long id;
    private UserResponse owner;
    private double square;
    private long lastUpdated;
    private String wkt;

    public List<Polygon> toEntities() {
        String coordsPart = wkt.substring(13, wkt.length()-1);
        List<String> listJsons = Arrays.stream(coordsPart.split("\\)\\),"))
                .map((it) -> {
                    String s = it.replaceAll("\\(\\(", "");
                    s = s.replaceAll("\\)\\)", "");
                    return s;})
                .map((it) -> it.trim().split(","))
                .map((pol) -> {
                    List<String> stringList = Arrays.stream(pol).
                            map((it) -> it.trim().split(" ")).
                            map((loc) -> String.format("{ \"latitude\": %s, \"longitude\": %s } ", loc[1], loc[0]))
                            .toList();
                    return "[\n    "
                            + String.join(",\n    ", stringList)
                            + "\n]";})
                .collect(Collectors.toList());

        List<Polygon> result = new ArrayList<>();
        listJsons.forEach((points) -> {
            Polygon poly = new Polygon();
            poly.id = this.id;
            poly.userId = this.owner.getUserId();
            poly.ownerName = this.owner.getUsername();
            poly.area = this.square;
            poly.lastUpdated = this.lastUpdated;
            poly.pointsJson = points;
            result.add(poly);
        });
        return result;
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
