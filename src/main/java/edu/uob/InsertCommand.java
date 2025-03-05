package edu.uob;

import java.util.HashMap;
import java.util.Map;

public class InsertCommand extends DBCommand {
    public String query(DBServer server) {
        if (tableNames.isEmpty() || values.isEmpty()) {
            return "[ERROR] Table name or values for insertion are missing.";
        }
        String tableName = tableNames.get(0);
        Table table = server.tables.get(tableName.toLowerCase());
        if (table == null) {
            return "[ERROR] Table '" + tableName + "' does not exist.";
        }
        int id = table.generateNextID();
        values.add(0, String.valueOf (id));
        if (table.insertRow(values)) {
            return "[OK] 1 row inserted into '" + tableName + "'.";
        } else {
            return "[ERROR] Failed to insert into '" + tableName + "' (column mismatch?).";
        }
    }

}
