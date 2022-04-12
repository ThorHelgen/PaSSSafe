package com.thorhelgen.paafe.data.database;

import android.content.Context;

import androidx.room.Room;

public final class DBWorker {
    private static volatile PasswordsDB database;

    public static PasswordsDB getDB(Context context) {
        PasswordsDB check = database;
        if (check != null) {
            return check;
        }
        synchronized (PasswordsDB.class) {
            if (database == null) {
                database = Room.databaseBuilder(context, PasswordsDB.class, "passwords").build();
            }
            return database;
        }
    }

    public static boolean makeRequest(Runnable func) {
        Thread requestThread = new Thread(func);
        requestThread.start();
        try {
            requestThread.join();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }
}
