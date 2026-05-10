package com.kyas.wolkandhold.data.api.requests;

import com.yandex.mapkit.geometry.Point;

import java.util.List;

public class PathRequest {

    private Long userId;
    private String username;
    private List<Point> path;

    public PathRequest(Long userId, String username, List<Point> path) {
        this.userId = userId;
        this.username = username;
        this.path = path;
    }

    @Override
    public String toString() {
        return "PathRequest{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", path=" + path +
                '}';
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
