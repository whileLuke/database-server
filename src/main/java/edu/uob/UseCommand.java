package edu.uob;

public class UseCommand extends DBCommand {
    @Override
    public String query(DBServer server) {
        if (server.useDatabase(DBName)) {
            return "[OK] Switched to database " + DBName + ".";
        } else {
            return "[ERROR] Database " + DBName + " does not exist.";
        }
    }

}
