package com.kyas.wolkandhold.data.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yandex.mapkit.geometry.Point;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "polygons")
public class Polygon {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long userId;
    public String ownerName;

    public String pointsJson;
    public double area;
    public long lastUpdated;

    public Polygon copyPolygon() {
        Polygon result = new Polygon();
        result.id = this.id;
        result.userId = this.userId;
        result.ownerName = this.ownerName;
        result.pointsJson = this.pointsJson;
        result.area = this.area;
        result.lastUpdated = this.lastUpdated;
        return result;
    }

    public List<Point> getPoints() {
        Gson gson = new Gson();
        return gson.fromJson(pointsJson, new TypeToken<List<Point>>(){}.getType());
    }
    @Override
    public String toString() {
        return "Polygon{" +
                "id=" + id +
                ", userId=" + userId +
                ", ownerName='" + ownerName + '\'' +
                ", pointsJson='" + pointsJson + '\'' +
                ", area=" + area +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}
