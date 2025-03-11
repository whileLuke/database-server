package edu.uob;

import java.io.IOException;

public class UseCommand extends DBCommand {
    @Override
    public String query(DBServer server) throws IOException {
        if (DBName == null || DBName.isEmpty()) {
            return "[ERROR] No database name specified.";
        }

        if (server.useDatabase(DBName)) {
            currentDB = server.getCurrentDB();
            tables = server.getTables();
            return "[OK] Switched to database " + DBName + ".";
        } else {
            return "[ERROR] Database " + DBName + " does not exist.";
        }
    }
}
