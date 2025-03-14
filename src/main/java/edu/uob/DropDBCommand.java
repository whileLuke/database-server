package edu.uob;

import java.io.IOException;

public class DropDBCommand extends DBCommand {
    @Override
    public String query() throws IOException {
        String errorMessage = errorChecker.checkIfDBNameProvided(dbName);
        if (errorMessage != null) return errorMessage;

        if (server.deleteDB(dbName)) return "[OK] Dropped database '" + dbName + "'.";
        else return "[ERROR] Database '" + dbName + "' does not exist.";
    }
}