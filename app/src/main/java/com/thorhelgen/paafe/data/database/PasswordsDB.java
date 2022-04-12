package com.thorhelgen.paafe.data.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = PasswordRecord.class, version = 1)
public abstract class PasswordsDB extends RoomDatabase {
    public abstract RecordDAO recordDAO();
}
