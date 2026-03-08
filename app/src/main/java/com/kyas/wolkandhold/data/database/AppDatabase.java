package com.kyas.wolkandhold.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.kyas.wolkandhold.data.database.dao.PlayerDao;
import com.kyas.wolkandhold.data.database.dao.PolygonDao;
import com.kyas.wolkandhold.data.database.dao.RouteDao;
import com.kyas.wolkandhold.data.database.dao.RoutePointDao;
import com.kyas.wolkandhold.data.database.entities.PlayerEntity;
import com.kyas.wolkandhold.data.database.entities.Polygon;
import com.kyas.wolkandhold.data.database.entities.Route;
import com.kyas.wolkandhold.data.database.entities.RoutePoint;

@Database(entities = {Route.class, RoutePoint.class, Polygon.class, PlayerEntity.class}, version = 8)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;
    public abstract RouteDao getRouteDao();
    public abstract RoutePointDao getRoutePointDao();
    public abstract PolygonDao getPolygonDao();
    public abstract PlayerDao getPlayerDao();
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "routes_db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
