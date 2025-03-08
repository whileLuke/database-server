package edu.uob;

import java.util.ArrayList;
import java.util.List;

public class SelectCommand extends DBCommand {
    private List<String> conditions = new ArrayList<>();

    public void setCondition(String condition) { this.conditions.add(condition); }

    @Override
    public String query(DBServer server) throws Exception {
        //loadTables(currentDB);
        if (tableNames.isEmpty()) return "[ERROR] Table name missing in SELECT query.";
        String tableName = tableNames.get(0).toLowerCase() /*+ ".tab"*/;
        Table table = tables.get(tableName);
        if (table == null) return "[ERROR] Table '" + tableName + "' does not exist.";
        if (conditions.isEmpty()) {
            if (columnNames.contains("*")) return table.selectAllColumns();
            else return table.selectColumns(columnNames);
        } else {
            if (columnNames.contains("*")) return table.selectAllColumns(conditions);
            else return table.selectColumns(columnNames, conditions);
        }
    }
}
