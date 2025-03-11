package edu.uob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class DBCommand {
    protected String commandType;
    protected String DBName;
    protected List<String> tableNames = new ArrayList<>();
    protected List<String> columnNames = new ArrayList<>();
    protected List<String> values = new ArrayList<>();
    protected List<String> conditions = new ArrayList<>();

    // References to important data from DBServer
    protected String currentDB;
    protected Map<String, Table> tables;
    protected DBServer server;

    public void setServer(DBServer server) {
        this.server = server;
        this.currentDB = server.getCurrentDB();
        this.tables = server.getTables();
    }

    public void setConditions(List<String> conditions) {
        this.conditions = conditions;
    }

    public abstract DBResponse execute() throws IOException;

    // Common utility methods

    protected String removeQuotes(String value) {
        if (value == null) return "";
        if ((value.startsWith("'") && value.endsWith("'")) ||
                (value.startsWith("\"") && value.endsWith("\""))) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    protected List<String> processValues(List<String> rawValues) {
        List<String> processedValues = new ArrayList<>();
        for (String value : rawValues) {
            processedValues.add(removeQuotes(value));
        }
        return processedValues;
    }

    protected Table getTable(String tableName) {
        if (tableName == null) return null;
        return tables.get(tableName.toLowerCase());
    }

    protected DBResponse validateDatabaseSelected() {
        return CommandValidator.validateDatabaseSelected(currentDB);
    }

    protected DBResponse validateTableExists(String tableName) {
        return CommandValidator.validateTableExists(tables, tableName.toLowerCase());
    }

    protected DBResponse validateTableNameProvided() {
        return CommandValidator.validateTableNameProvided(tableNames);
    }

    protected boolean saveCurrentDB() throws IOException {
        return server.saveCurrentDB();
    }
}
