package edu.uob;

import java.io.IOException;

public class CreateDatabaseCommand extends DBCommand {
    @Override
    public DBResponse query() throws IOException {
        DBResponse validationResponse = CommandValidator.validateDatabaseNameProvided(DBName);
        if (validationResponse != null) return validationResponse;
        if (NotAllowedWords.isNotAllowed(DBName)) return DBResponse.error("Cannot use reserved word '" + DBName + "' as a database name.");
        if (server.createDatabase(DBName)) return DBResponse.success("Database '" + DBName + "' created.");
        else return DBResponse.error("Failed to create database '" + DBName + "'. It may already exist.");
    }
}
