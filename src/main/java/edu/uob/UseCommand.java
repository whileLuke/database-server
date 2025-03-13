package edu.uob;

import java.io.IOException;

public class UseCommand extends DBCommand {
    @Override
    public String query() throws IOException {
        String error = errorChecker.validateDatabaseNameProvided(DBName);
        if (error != null) return error;
        if (server.useDatabase(DBName)) return "[OK] Using database '" + DBName + "'.";
        else return "[ERROR] Database '" + DBName + "' does not exist.";
    }
}
