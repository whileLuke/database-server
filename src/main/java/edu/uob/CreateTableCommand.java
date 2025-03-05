package edu.uob;

import java.util.ArrayList;

public class CreateTableCommand extends DBCommand {
    @Override
    public String query(DBServer server) {
        System.out.println("Create table test");
        if (tableNames.isEmpty()) return "[ERROR] Table name not specified.";
        if (tableNames.size() != 1) return "[ERROR] Incorrect number of table names selected.";
        if (server.currentDB == null) return "[ERROR] No database selected. Use 'USE database;' first.";
        String tableName = tableNames.get(0).toLowerCase();
        if (server.tables.containsKey(tableName)) return "[ERROR] Table already exists.";
        System.out.println("Create table test 2");
        //if(server.createTable(tableName, columnNames)){
        //    server.saveCurrentDB();
        //    return "[OK] Created table '" + tableName + "'.";
        //} else {
        //    return "[ERROR] Could not create table '" + tableName + "'.";
        //}
        //THIS COULD MESS IT UP
        if (!columnNames.contains("id")) {
            columnNames.add(0, "id");
        }
        Table newTable = new Table(new ArrayList<>(columnNames));
        server.tables.put(tableName, newTable);
        if (server.saveCurrentDB()) {
            return "[OK] Created table '" + tableName + "'.";
        } else {
            server.tables.remove(tableName);
            return "[ERROR] Failed to save table '" + tableName + "' to disk.";
        }
    }

    //ALDO NEED TO MAKE IT WORK FOR TABLES W MULTIPLE COLUMNS
}
