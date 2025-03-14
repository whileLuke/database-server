package edu.uob;

import java.io.IOException;

public class DropDBCommand extends DBCommand {
    @Override
    public String query() throws IOException {
        String errorMessage = errorChecker.checkIfDBNameProvided(DBName);
        if (errorMessage != null) return errorMessage;

        if (server.deleteDB(DBName)) return "[OK] Dropped database '" + DBName + "'.";
        else return "[ERROR] Database '" + DBName + "' does not exist.";
    }
}