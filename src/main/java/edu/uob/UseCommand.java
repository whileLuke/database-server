package edu.uob;

import java.io.IOException;

public class UseCommand extends DBCommand {
    @Override
    public String query() throws IOException {
        String errorMessage = errorChecker.checkIfDBNameProvided(dbName);

        if (errorMessage != null) return errorMessage;

        if (server.useDB(dbName)) return "[OK] Using database '" + dbName + "'.";
        else return "[ERROR] Database '" + dbName + "' does not exist.";
    }
}
