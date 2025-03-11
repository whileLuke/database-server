package edu.uob;

import java.io.IOException;

public class CreateDatabaseCommand extends DBCommand {
    @Override
    public String query(DBServer server) throws IOException {
        if (DBName == null || DBName.isEmpty()) {
            return "[ERROR] No database name specified.";
        }

        if (server.createDatabase(DBName)) {
            return "[OK] Database '" + DBName + "' created.";
        } else {
            return "[ERROR] Failed to create database '" + DBName + "'. It may already exist.";
        }
    }
}
