package com.kyas.wolkandhold.data.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.Query;

import com.kyas.wolkandhold.data.database.entities.Route;

import java.util.List;

@Dao
public interface RouteDao {
    @Insert
    long insert(Route route);

    @Delete
    void delete(Route route);
    // CHANGE: Удаление по id для удобства каскадного удаления из репозитория
    @Query("DELETE FROM routes WHERE id = :routeId")
    void deleteById(long routeId);

    // CHANGE: Обновление маршрута (например, имени)
    @Update
    void update(Route route);
    @Query("SELECT * FROM routes")
    List<Route> getAllRoutes();
}
