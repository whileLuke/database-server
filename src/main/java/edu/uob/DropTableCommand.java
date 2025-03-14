package edu.uob;

import java.io.File;
import java.io.IOException;

public class DropTableCommand extends DBCommand {
    @Override
    public String query() throws IOException {
        String errorMessage = errorChecker.checkTableFunctionality(tableNames);
        if (errorMessage != null) return errorMessage;

        String tableName = tableNames.get(0).toLowerCase();
        File tableFile = new File(server.getStorageFolderPath() + File.separator + currentDB, tableName + ".tab");

        if (tableFile.delete()) {
            tables.remove(tableName);
            saveCurrentDB();
            return "[OK] Dropped table '" + tableName + "'.";
        } else return "[ERROR] Failed to drop table '" + tableName + "'.";
    }
}