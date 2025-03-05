package edu.uob;

public class CreateDatabaseCommand extends DBCommand {
    //@Override
    public String query(DBServer server) {
        System.out.println("Test 2");
        if (DBName == null) return "[ERROR] Database name not specified.";
        if(server.createDatabase(DBName)){
            server.saveCurrentDB();
            return "[OK] Created database '" + DBName + "'.";
        } else {
            return "[ERROR] Could not create database '" + DBName + "'.";
        }
    }
}
