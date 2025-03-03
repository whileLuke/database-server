package edu.uob;

public class UseCommand extends DBCommand {
    public String query(DBServer s) {
        if (s.useDatabase(DBName)) {
            return "[OK] Switched to database " + DBName + ".";
        } else {
            return "[ERROR] Database " + DBName + " does not exist.";
        }
    }

}
