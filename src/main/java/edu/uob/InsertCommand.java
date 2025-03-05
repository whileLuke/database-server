package edu.uob;

import java.util.ArrayList;

public class InsertCommand extends DBCommand {
    @Override
    public String query(DBServer server) {
        if (tableNames.isEmpty() || values.isEmpty()) {
            return "[ERROR] Table name or values for insertion are missing.";
        }
        String tableName = tableNames.get(0).toLowerCase() /*+ ".tab"*/;
        Table table = server.tables.get(tableName);
        if (table == null) {
            return "[ERROR] Table '" + tableName + "' does not exist.";
        }
        int id = table.generateNextID();
        ArrayList<String> rowValues = new ArrayList<>(values);
        /*if (!table.getColumns().get(0).equalsIgnoreCase("id")) {
            rowValues.add(0, String.valueOf(id));
        }*/
        rowValues.add(0, String.valueOf (id));
        if (table.insertRow(rowValues)) {
            server.saveCurrentDB();
            return "[OK] 1 row inserted into '" + tableName + "'.";
        } else {
            return "[ERROR] Failed to insert into '" + tableName + "'. Column count mismatch: expected " +
                    table.getColumns().size() + " columns, got " + rowValues.size() + " values.";
        }
    }

}
