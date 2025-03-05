package edu.uob;

public class CreateTableCommand extends DBCommand {
    //@Override
    public String query(DBServer server) {
        System.out.println("Create table test");
        if (tableNames.isEmpty() || columnNames.isEmpty()) return "[ERROR] Table names or column names have not been defined.";
        if (tableNames.size() != 1) return "[ERROR] Incorrect number of table names selected.";
        String tableName = tableNames.get(0);
        if (server.tables.containsKey(tableName.toLowerCase())) return "[ERROR] Table already exists.";
        System.out.println("Create table test 2");
        if(server.createTable(tableName, columnNames)){
            server.saveCurrentDB();
            return "[OK] Created table '" + tableName + "'.";
        } else {
            return "[ERROR] Could not create table '" + tableName + "'.";
        }
    }

    //ALDO NEED TO MAKE IT WORK FOR TABLES W MULTIPLE COLUMNS
}
