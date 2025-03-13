package edu.uob;

import java.io.IOException;

public class CreateDatabaseCommand extends DBCommand {
    @Override
    public String query() throws IOException {
        String errorMessage = errorChecker.validateDatabaseNameProvided(DBName);
        if (errorMessage != null) return errorMessage;
        errorMessage = errorChecker.CheckIfReservedWord(DBName);
        if (errorMessage != null) return errorMessage;
        if (server.createDatabase(DBName)) return "[OK] Database '" + DBName + "' created.";
        else return "[ERROR] Failed to create database '" + DBName + "'. Check if it already exists.";
    }
}
