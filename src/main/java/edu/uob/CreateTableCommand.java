package edu.uob;

public class CreateTableCommand extends DBCommand {
    @Override
    public String query(DBServer server) {
        if (tableNames.size() != 1) return "[ERROR] Incorrect number of table names selected.";
        if(server.createTable(tableNames.get(0), columnNames)){
            return "[OK] Created table '" + tableNames + "'.";
        } else {
            return "[ERROR] Could not create table '" + tableNames + "'.";
        }
    }

    //ALDO NEED TO MAKE IT WORK FOR TABLES W MULTIPLE COLUMNS
}
