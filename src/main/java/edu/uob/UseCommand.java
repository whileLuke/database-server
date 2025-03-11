package edu.uob;

import java.io.IOException;

public class UseCommand extends DBCommand {
    @Override
    public DBResponse query() throws IOException {
        DBResponse validationResponse = CommandValidator.validateDatabaseNameProvided(DBName);
        if (validationResponse != null) return validationResponse;

        if (server.useDatabase(DBName)) return DBResponse.success("Using database '" + DBName + "'.");
        else return DBResponse.error("Database '" + DBName + "' does not exist.");
    }
}
