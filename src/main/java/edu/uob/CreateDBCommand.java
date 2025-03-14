package edu.uob;

import java.io.IOException;

public class CreateDBCommand extends DBCommand {
    @Override
    public String query() throws IOException {
        String errorMessage = errorChecker.checkIfDBNameProvided(dbName);
        if (errorMessage != null) return errorMessage;

        errorMessage = errorChecker.checkIfReservedWord(dbName);
        if (errorMessage != null) return errorMessage;

        if (server.createDB(dbName)) return "[OK] Database '" + dbName + "' created.";
        else return "[ERROR] Failed to create database '" + dbName + "'. Check if it already exists.";
    }
}
