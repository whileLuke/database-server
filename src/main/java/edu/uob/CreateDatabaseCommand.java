package edu.uob;

import java.io.IOException;

public class CreateDatabaseCommand extends DBCommand {
    @Override
    public String query() throws IOException {
        if (DBName == null || DBName.isEmpty()) return "[ERROR] You have not provided a name for the database.";
        if (ReservedWords.isNotAllowed(DBName)) return "[ERROR] Cannot use reserved word '" + DBName + "' as a database name.";
        if (server.createDatabase(DBName)) return "[OK] Database '" + DBName + "' created.";
        else return "[ERROR] Failed to create database '" + DBName + "'. Check if it already exists.";
    }
}
