package com.kyas.wolkandhold.data.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Upsert;


import com.kyas.wolkandhold.data.database.entities.PlayerEntity;

import java.util.List;

@Dao
public interface PlayerDao {
    @Insert
    long addPlayer(PlayerEntity playerEntity);

    @Update
    void updatePlayer(PlayerEntity playerEntity);

    @Upsert
    long upsertPlayer(PlayerEntity playerEntity);

    @Query("SELECT * FROM players WHERE playerId = :id LIMIT 1")
    PlayerEntity getById(long id);

    @Query("Select * From players")
    List<PlayerEntity> getAllPlayers();

    @Query("Select * From players")
    LiveData<List<PlayerEntity>> getAllPlayersLive();
}
