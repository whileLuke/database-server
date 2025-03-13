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
    protected String currentDB;
    protected Map<String, DBTable> tables;
    protected DBServer server;
    protected CommandErrorChecker errorChecker;

    public void setServer(DBServer server) {
        this.server = server;
        this.currentDB = server.getCurrentDB();
        this.tables = server.getTables();
        this.errorChecker = new CommandErrorChecker(currentDB, tables);
    }

    public void setConditions(List<String> conditions) {
        this.conditions = conditions;
    }

    public abstract String query() throws IOException;

    protected String removeQuotes(String stringToEdit) {
        if (stringToEdit == null) return "";
        if (stringToEdit.startsWith("'") && stringToEdit.endsWith("'")) {
            return stringToEdit.substring(1, stringToEdit.length() - 1);
        }
        return stringToEdit;
    }

    protected List<String> processValues(List<String> rawValues) {
        List<String> processedValues = new ArrayList<>();
        for (String value : rawValues) {
            processedValues.add(removeQuotes(value));
        }
        return processedValues;
    }

    protected DBTable getTable(String tableName) {
        if (tableName == null) return null;
        return tables.get(tableName.toLowerCase());
    }

    protected boolean saveCurrentDB() throws IOException {
        return server.saveCurrentDB();
    }
}
