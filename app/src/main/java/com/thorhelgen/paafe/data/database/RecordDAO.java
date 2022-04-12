package com.thorhelgen.paafe.data.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface RecordDAO {

    @Query("SELECT COUNT(*) FROM PasswordRecord")
    int count();

    @Query("SELECT * FROM PasswordRecord ORDER BY recId LIMIT 1 OFFSET :index")
    PasswordRecord getRecordByIndex(long index);

    @Insert
    void insert(PasswordRecord rec);

    @Update
    void update(PasswordRecord rec);

    @Query("DELETE FROM PasswordRecord WHERE recId = :recId")
    void delete(long recId);
}
