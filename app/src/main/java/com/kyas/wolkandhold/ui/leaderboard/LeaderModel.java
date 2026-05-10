package com.kyas.wolkandhold.ui.leaderboard;

import android.graphics.Bitmap;

public class LeaderModel {

    private String username;
    private double totalSquare;
    private Bitmap avatar;
    private long id;

    @Override
    public String toString() {
        return "LeaderModel{" +
                "username='" + username + '\'' +
                ", totalSquare=" + totalSquare +
                ", avatar=" + avatar +
                ", id=" + id +
                '}';
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public double getTotalSquare() {
        return totalSquare;
    }

    public void setTotalSquare(double totalSquare) {
        this.totalSquare = totalSquare;
    }

    public Bitmap getAvatar() {
        return avatar;
    }

    public void setAvatar(Bitmap avatar) {
        this.avatar = avatar;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
