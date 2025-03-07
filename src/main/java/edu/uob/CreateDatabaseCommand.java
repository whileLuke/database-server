package edu.uob;

import java.io.IOException;

public class CreateDatabaseCommand extends DBCommand {
    //@Override
    public String query(DBServer server) throws IOException {
        System.out.println("Test 2");
        if (DBName == null) return "[ERROR] Database name not specified.";
        if(createDatabase(DBName)){
            saveCurrentDB();
            return "[OK] Created database '" + DBName + "'.";
        } else {
            return "[ERROR] Could not create database '" + DBName + "'.";
        }
    }
}
