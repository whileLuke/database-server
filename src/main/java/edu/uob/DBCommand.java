package edu.uob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class DBCommand {
    public String commandType;
    public String DBName;
    public List<String> tableNames = new ArrayList<>();
    public List<String> columnNames = new ArrayList<>();
    public List<String> values = new ArrayList<>();

    // References to important data from DBServer
    protected String currentDB;
    protected Map<String, Table> tables;
    protected DBServer server;

    public void setServer(DBServer server) {
        this.server = server;
        this.currentDB = server.getCurrentDB();
        this.tables = server.getTables();
    }

    public abstract String query(DBServer server) throws IOException;

    // Helper methods that were previously in DBServer but might be needed by commands
    protected boolean saveCurrentDB() throws IOException {
        return server.saveCurrentDB();
    }

    protected boolean loadTables(String DBName) throws IOException {
        return server.loadTables(DBName);
    }

    protected boolean createDatabase(String DBName) {
        return server.createDatabase(DBName);
    }
}
