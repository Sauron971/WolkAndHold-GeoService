package com.kyas.wolkandhold.data.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "players")
public class PlayerEntity {

    @PrimaryKey()
    public long playerId;
    public String playerName;

    public double lat;
    public double lon;
    public long lastUpdated;

    public PlayerEntity(long playerId, String playerName, double lat, double lon, long lastUpdated) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.lat = lat;
        this.lon = lon;
        this.lastUpdated = lastUpdated;
    }

    @NonNull
    @Override
    public String toString() {
        return "PlayerEntity{" +
                "playerId=" + playerId +
                ", playerName='" + playerName + '\'' +
                ", lat=" + lat +
                ", lon=" + lon +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}
