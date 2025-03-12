package edu.uob;

import java.io.IOException;

public class CreateDatabaseCommand extends DBCommand {
    @Override
    public String query() throws IOException {
        if (DBName.isEmpty()) return "[ERROR] You have not provided a name for the database.";
        if (NotAllowedWords.isNotAllowed(DBName)) return "[ERROR] Cannot use reserved word '" + DBName + "' as a database name.");
        if (server.createDatabase(DBName)) return DBResponse.success("Database '" + DBName + "' created.");
        else return "[ERROR] Failed to create database '" + DBName + "'. It may already exist.");
    }
}
