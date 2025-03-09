package edu.uob;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
//INSERT no longer works on a restart. can only insert into a recently created table.
//ONLY I8SSUE IS BC LOADTABLES IS UNCOMMENTED I CAN PROB INSERT INTO ANY TABLE EVEN FROM DIFF DBS
public class InsertCommand extends DBCommand {
    @Override
    public String query(DBServer server) throws IOException {
        //loadTables(currentDB);
        if (tableNames.isEmpty() || values.isEmpty()) {
            return "[ERROR] Table name or values for insertion are missing.";
        }
        String tableName = tableNames.get(0).toLowerCase() /*+ ".tab"*/;
        //REDO THIS. LIKE HOW I REDID DROP TABLE.
        File tableFile = new File(storageFolderPath + File.separator + currentDB, tableName.toLowerCase() + ".tab");
        Table table = tables.get(tableName);
        if (table == null) {
            return "[ERROR] Table '" + tableName + "' does not exist.";
        }
        //server.tables.put(tableName, table);
                //server.tables.get(tableName);
        //REDO THIS. LIKE HOW I REDID DROP TABLE.
        //basically server.tables is NEVERRRR containing tables annoyingly
        //if (!tableFile.exists()) {
        //   return "[ERROR] Table '" + tableName + "' does not exist.";
        //}
        int id = table.generateNextID();
        //int id = server.tables.get(0);
        ArrayList<String> rowValues = new ArrayList<>(values);
        /*if (!table.getColumns().get(0).equalsIgnoreCase("id")) {
            rowValues.add(0, String.valueOf(id));
        }*/
        rowValues.add(0, String.valueOf (id));
        if (table.insertRow(rowValues)) {
            saveCurrentDB();
            return "[OK] 1 row inserted into '" + tableName + "'.";
        } else {
            return "[ERROR] Failed to insert into '" + tableName + "'. Column count mismatch: expected " +
                    (table.getColumns().size() - 1) + " columns, got " + (rowValues.size() - 1) + " values.";
        }
    }

}
