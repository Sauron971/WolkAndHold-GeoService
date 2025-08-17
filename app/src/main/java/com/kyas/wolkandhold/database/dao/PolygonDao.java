package com.kyas.wolkandhold.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.kyas.wolkandhold.database.entities.Polygon;
import com.kyas.wolkandhold.database.entities.Route;

import java.util.List;

@Dao
public interface PolygonDao {

    @Insert
    long addPolygon(Polygon polygon);

    @Update
    int updatePolygon(Polygon polygon);

    @Query("Select * From polygons Where userId = :userId")
    List<Polygon> getPolygonsByUser(long userId);

    @Query("Select * From polygons")
    LiveData<List<Polygon>> getAllPolygons();
}
