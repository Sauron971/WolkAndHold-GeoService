package com.kyas.wolkandhold.data.models;


public class PlayerModel {

    public long playerId;
    public String playerName;

    public double lat;
    public double lon;
    public boolean isCapture;
    public long lastUpdated;

    public PlayerModel(long playerId, String playerName, double lat, double lon, boolean isCapture, long lastUpdated) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.lat = lat;
        this.lon = lon;
        this.isCapture = isCapture;
        this.lastUpdated = lastUpdated;
    }

    @Override
    public String toString() {
        return "PlayerModel{" +
                "playerId=" + playerId +
                ", playerName='" + playerName + '\'' +
                ", lat=" + lat +
                ", lon=" + lon +
                ", isCapture=" + isCapture +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}
