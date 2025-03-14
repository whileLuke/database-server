package edu.uob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class DBCommand {
    protected DBServer server;
    protected String currentDB;
    protected String commandType;
    protected String dbName;
    protected Map<String, DBTable> tables;
    protected CommandErrorChecker errorChecker;
    protected InputTokeniser inputTokeniser;
    protected List<String> tableNames = new ArrayList<>();
    protected List<String> columnNames = new ArrayList<>();
    protected List<String> values = new ArrayList<>();
    protected List<String> conditions = new ArrayList<>();


    public void setServer(DBServer server) {
        this.server = server;
        this.currentDB = server.getCurrentDB();
        this.tables = server.getTables();
        this.errorChecker = new CommandErrorChecker(currentDB, tables);
    }

    public abstract String query() throws IOException;

    public void setConditions(List<String> conditions) {
        this.conditions = conditions;
    }

    protected boolean saveCurrentDB() throws IOException { return server.saveCurrentDB(); }

    protected DBTable getTable(String tableName) { return tables.get(tableName.toLowerCase()); }

    protected String removeQuotes(String stringToEdit) {
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
}
