package edu.uob;

import java.io.IOException;

public class UseCommand extends DBCommand {
    @Override
    public String query() throws IOException {
        String error = errorChecker.checkIfDBNameProvided(DBName);
        if (error != null) return error;
        if (server.useDatabase(DBName)) return "[OK] Using database '" + DBName + "'.";
        else return "[ERROR] Database '" + DBName + "' does not exist.";
    }
}
