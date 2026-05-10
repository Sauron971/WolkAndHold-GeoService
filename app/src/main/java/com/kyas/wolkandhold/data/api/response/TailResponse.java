package com.kyas.wolkandhold.data.api.response;

import com.yandex.mapkit.geometry.Point;

import java.util.List;

public class TailResponse {

    private Long userId;

    private String username;

    private List<Point> path;

    public TailResponse(Long userId, String username, List<Point> path) {
        this.userId = userId;
        this.username = username;
        this.path = path;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<Point> getPath() {
        return path;
    }

    public void setPath(List<Point> path) {
        this.path = path;
    }
}

