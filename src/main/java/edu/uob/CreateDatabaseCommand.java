package edu.uob;

public class CreateDatabaseCommand extends DBCommand {
    public String query(DBServer s) {
        if (DBName == null) return "[ERROR] Database name not specified.";
        if(s.createDatabase(DBName)){
            return "[OK] Created database '" + DBName + "'.";
        } else {
            return "[ERROR] Could not create database '" + DBName + "'.";
        }
    }
}
