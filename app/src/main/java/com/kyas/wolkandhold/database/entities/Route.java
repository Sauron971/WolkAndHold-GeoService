package com.kyas.wolkandhold.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Route {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String name;
    public long startTime;
    public long endTime;
}
