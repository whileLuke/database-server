package edu.uob;

import java.io.IOException;

public class AlterCommand extends DBCommand {
    @Override
    public DBResponse query() throws IOException {
        DBResponse validationResponse;
        if ((validationResponse = validateDatabaseSelected()) != null) return validationResponse;

        if (tableNames.isEmpty() || columnNames.isEmpty() || commandType == null) return DBResponse.error("Your ALTER command is incorrect - you may potentially not have provided a table name or any column names.");

        String tableName = tableNames.get(0).toLowerCase();
        if ((validationResponse = validateTableExists(tableName)) != null) return validationResponse;

        String columnName = columnNames.get(0);
        if ((validationResponse = CommandValidator.validateNotIdColumn(columnName)) != null) return validationResponse;

        if (NotAllowedWords.isNotAllowed(columnName)) return DBResponse.error("The word '" + columnName + "' is not allowed as a column name in a table - it's a reserved SQL keyword - please try again without using reserved keywords.");

        Table table = getTable(tableName);

        if (commandType.equalsIgnoreCase("ADD")) {
            if (!table.addColumn(columnName)) return DBResponse.error("That column - '" + columnName + "' - already exists in the table you're trying to add it into.");
        }
        else if (commandType.equalsIgnoreCase("DROP")) {
            if (!table.dropColumn(columnName)) return DBResponse.error("That column - '" + columnName + "' - doesn't exist in the table you're trying to drop it from.");
        }
        else return DBResponse.error("Your ALTER command is incorrect. You did not specify either 'ADD' or 'DROP', which are the only two things an ALTER command can do.");

        if (saveCurrentDB()) return DBResponse.success("You have successfully altered the table" + tableName + ".");
        else return DBResponse.error("The altered table could not be saved to your file system, something has gone wrong - check saving is working correctly.");
    }
}
