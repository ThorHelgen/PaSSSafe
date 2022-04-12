package com.thorhelgen.paafe.data.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(indices = {
//        @Index(name = "idx", value = "idx", unique = true)
})
public class PasswordRecord {
    @PrimaryKey(autoGenerate = true)
    public long recId;

    @ColumnInfo(name = "description")
    public String description;

    @ColumnInfo(name = "log")
    public String log;

    @ColumnInfo(name = "pass")
    public String pass;


    public PasswordRecord() {

    }

    public PasswordRecord(String desc, String log, String pass) {
        this.description = desc;
        this.log = log;
        this.pass = pass;
    }

    public PasswordRecord(long id, String desc, String log, String pass) {
        recId = id;
        description = desc;
        this.log = log;
        this.pass = pass;
    }
}
