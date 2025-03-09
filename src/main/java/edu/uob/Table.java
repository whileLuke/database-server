package edu.uob;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.stream.Collectors;

public class Table implements Serializable {
    private static final String FILE_EXTENSION = ".tab";
    private static final long serialVersionUID = 1L;
    private final List<String> columns;
    private final List<List<String>> rows;

    public Table(List<String> columns) {
        this.columns = new ArrayList<>(columns);
        this.rows = new ArrayList<>();
    }

    public List<String> getColumns() {
        return columns;
    }

    public List<List<String>> getRows() {
        return rows;
    }

    public void addRow(List<String> row) {
        if (row.size() != columns.size()) {
            throw new IllegalArgumentException("The row does not match the table's size.");
        }
        rows.add(row);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.join(",", columns)).append("\n");
        for (List<String> row : rows) {
            sb.append(String.join(",", row)).append("\n");
        }
        return sb.toString();
    }

    public boolean addColumn(String columnName) {
        if (columns.contains(columnName)) return false;
        columns.add(columnName);
        for (List<String> row : rows) {
            row.add("NULL");
        }
        return true;
    }

    public boolean dropColumn(String columnName) {
        int columnIndex = columns.indexOf(columnName);
        if (columnIndex == -1) return false;
        columns.remove(columnIndex);
        for (List<String> row : rows) {
            row.remove(columnIndex);
        }
        return true;
    }

    public boolean insertRow(List<String> values) {
        if (values.size() != columns.size()) return false;
        //maybe add at x index. have a counter of which row imon maybe?
        rows.add(new ArrayList<>(values));
        System.out.println("rows is =" + rows);
        return true;
    }

    public String selectAllColumns() {
        StringBuilder result = new StringBuilder();
        result.append(String.join(" | ", columns)).append("\n");
        for (List<String> row : rows) {
            result.append(String.join(" | ", row)).append("\n");
        }
        return result.toString();
    }

    public String selectAllColumns(List<String> conditions) throws Exception {
        // Handle null or empty conditions
        if (conditions == null || conditions.isEmpty()) {
            // If no conditions are provided, return all rows
            return formatRows(columns, rows);
        }

        // Create a list to store the rows that match the conditions
        List<List<String>> selectedRows = new ArrayList<>();

        // Iterate over all rows and apply the conditions
        for (List<String> row : rows) {
            if (isRowMatchConditions(row, conditions, columns)) {
                selectedRows.add(row); // Add matching rows to the selectedRows list
            }
        }

        // If no rows match, return only the header row (column names)
        if (selectedRows.isEmpty()) {
            return String.join("\t", columns) + "\n";
        }

        // Format and return the selected rows along with column names
        return formatRows(columns, selectedRows);
    }


    public List<List<String>> selectRowsWithoutConditions(List<String> selectedColumns) {
        List<List<String>> result = new ArrayList<>();
        List<Integer> columnIndexes = getColumnIndexes(selectedColumns); // Get indexes of the selected columns

        for (List<String> row : rows) {
            // Add only the values in the requested columns
            result.add(columnIndexes.stream().map(row::get).collect(Collectors.toList()));
        }

        return result;
    }

    public List<List<String>> selectRowsWithConditions(List<String> selectedColumns, List<String> conditions) throws Exception {
        List<List<String>> filteredRows = new ArrayList<>();
        for (List<String> row : rows) {
            if (isRowMatchConditions(row, conditions, columns)) {  // Pass the 'columns' list as the third argument
                filteredRows.add(getSelectedColumns(row, selectedColumns));
            }
        }
        return filteredRows;
    }

    private List<String> getSelectedColumns(List<String> row, List<String> selectedColumns) {
        List<Integer> selectedIndexes = getColumnIndexes(selectedColumns);
        List<String> selectedRow = new ArrayList<>();
        for (int index : selectedIndexes) {
            selectedRow.add(row.get(index));
        }
        return selectedRow;
    }



   /* private List<Integer> getColumnIndexes(List<String> selectedColumns) {
        List<Integer> indexes = new ArrayList<>();
        for (String column : selectedColumns) {
            indexes.add(columns.indexOf(column));
        }
        return indexes;
    } */


    private String formatRows(List<String> columns, List<List<String>> selectedRows) {
        StringBuilder result = new StringBuilder();

        // Add column headers as the first line
        result.append(String.join("\t", columns)).append("\n");

        // Add each row of data
        for (List<String> row : selectedRows) {
            result.append(String.join("\t", row)).append("\n");
        }

        // Remove the final newline for cleaner output, if there is any data
        if (result.length() > 0) {
            result.setLength(result.length() - 1); // Trim last '\n'
        }
        return result.toString();
    }

    public String selectColumns(List<String> selectedColumns) {
        StringBuilder result = new StringBuilder();
        List<Integer> columnIndexes = new ArrayList<>();
        for (String column : selectedColumns) {
            int index = columns.indexOf(column);
            if (index == -1) {
                return "[ERROR] Column '" + column + "' does not exist.";
            }
            columnIndexes.add(index);
        }
        result.append(String.join(" | ", selectedColumns)).append("\n");
        for (List<String> row : rows) {
            List<String> selectedRowValues = columnIndexes.stream()
                    .map(row::get)
                    .collect(Collectors.toList());
            result.append(String.join(" | ", selectedRowValues)).append("\n");
        }
        return result.toString();
    }

    public String selectColumns(List<String> selectedColumns, List<String> conditions) throws Exception {
        StringBuilder result = new StringBuilder();
        List<Integer> columnIndexes = getColumnIndexes(selectedColumns);
        if (columnIndexes.isEmpty()) {
            return "[ERROR] Columns '" + String.join(", ", selectedColumns) + "' do not exist.";
        }

        result.append(String.join(" | ", selectedColumns)).append("\n");

        for (List<String> row : rows) {
            if (isRowMatchConditions(row, conditions, columns)) {
                List<String> selectedRowValues = columnIndexes.stream()
                        .map(row::get)
                        .collect(Collectors.toList());
                result.append(String.join(" | ", selectedRowValues)).append("\n");
            }
        }
        return "[OK] " + result;
    }

    private List<Integer> getColumnIndexes(List<String> columnNames) {
        List<Integer> columnIndexes = new ArrayList<>();

        for (String columnName : columnNames) {
            int index = columns.indexOf(columnName);
            if (index == -1) {
                return new ArrayList<>();  // If we find a single non-existing column, we return an empty list
            }
            columnIndexes.add(index);
        }

        return columnIndexes;
    }

    private boolean isRowMatchConditions(List<String> row, List<String> conditions, List<String> columns) throws Exception {
        for (String condition : conditions) {
            String[] parts = splitCondition(condition);
            if (parts == null || parts.length != 3) {
                throw new Exception("Invalid condition format: " + condition);
            }

            String columnName = parts[0].trim();
            String operator = parts[1].trim();
            String value = parts[2].trim();

            // Get the column index for the column being checked
            int columnIndex = columns.indexOf(columnName);
            if (columnIndex == -1) {
                throw new Exception("Column not found: " + columnName);
            }

            // Get the value of the row at the column index
            String rowValue = row.get(columnIndex);

            // Check if the row satisfies the condition
            if (!evaluateCondition(rowValue, value, operator)) {
                return false; // If any condition fails, the row does not match
            }
        }
        return true; // All conditions matched
    }

    private String[] splitCondition(String condition) {
        // Define a list of valid operators, ordered by length to handle multi-character operators first
        String[] operators = {"==", "!=", ">=", "<=", ">", "<", "LIKE"};

        // Check each operator to see if it exists in the condition string
        for (String operator : operators) {
            int index = condition.indexOf(operator);
            if (index != -1) {
                // Split the condition into column, operator, and value
                String column = condition.substring(0, index).trim();
                String value = condition.substring(index + operator.length()).trim();
                return new String[]{column, operator, value};
            }
        }

        // If no operator is found, return null
        return null;
    }

    private boolean evaluateCondition(String rowValue, String conditionValue, String operator) {
        // Remove surrounding quotes from conditionValue if needed
        if (conditionValue.startsWith("\"") || conditionValue.startsWith("'")) {
            conditionValue = conditionValue.substring(1, conditionValue.length() - 1);
        }

        // Perform the comparison based on the operator
        switch (operator) {
            case "==":
                return rowValue.equals(conditionValue);
            case "!=":
                return !rowValue.equals(conditionValue);
            case ">":
                return Double.parseDouble(rowValue) > Double.parseDouble(conditionValue);
            case "<":
                return Double.parseDouble(rowValue) < Double.parseDouble(conditionValue);
            case ">=":
                return Double.parseDouble(rowValue) >= Double.parseDouble(conditionValue);
            case "<=":
                return Double.parseDouble(rowValue) <= Double.parseDouble(conditionValue);
            case "LIKE":
                return rowValue.contains(conditionValue); // Simple "contains" logic for LIKE
            default:
                return false;
        }
    }



    public boolean updateRows(List<String> columnNamesToUpdate, List<String> newValues) {
        if (columnNamesToUpdate.size() != newValues.size()) {
            return false;
        }
        List<Integer> updateIndexes = new ArrayList<>();
        for (String columnName : columnNamesToUpdate) {
            int index = columns.indexOf(columnName);
            if (index == -1) {
                return false;
            }
            updateIndexes.add(index);
        }
        for (List<String> row : rows) {
            for (int i = 0; i < updateIndexes.size(); i++) {
                row.set(updateIndexes.get(i), newValues.get(i));
            }
        }
        return true;
    }

    public int generateNextID() {
        int idColumnIndex = columns.indexOf("id");
        if (idColumnIndex == -1) {
            // Add id column if it doesn't exist
            columns.add(0, "id");
            for (List<String> row : rows) {
                row.add(0, "0");
            }
            idColumnIndex = 0;
        }
        int maxID = 0;
        for (List<String> row : rows) {
            try {
                int currentID = Integer.parseInt(row.get(idColumnIndex));
                if (currentID > maxID) maxID = currentID;
            } catch (NumberFormatException ignored) {
                // Skip non-numeric values
            }
        }
        return maxID + 1;
    }

    public void saveToFile(String databasePath, String tableName) throws IOException {
        File tableFile = new File(databasePath, tableName + FILE_EXTENSION);

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(tableFile))) {
            // Writing columns
            writer.write(String.join("  ", columns));
            writer.newLine();

            // Writing rows
            for(List<String> row : rows) {
                writer.write(String.join("  ", row));
                writer.newLine();
            }
        }
    }

    private void debugLog(String message) {
        // Check if debugging is enabled before printing the message.
        if (isDebugEnabled()) {
            System.out.println(message);
        }
    }

    //private Logger getLogger() {
        // Get the logger
    //    return Logger.getLogger(getClass().getName());
    //}

    private boolean isDebugEnabled() {
        // Determine if debugging is enabled.
        return true;
    }



    //DO A BUFFEREDREADER INSTEAD.

    public static Table loadFromFile(String databasePath, String tableName) {
        File tableFile = new File(databasePath, tableName + FILE_EXTENSION); // Ensure consistency with the extension
        if (!tableFile.exists()) {
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(tableFile))) {
            List<String> columnNames = new ArrayList<>();
            List<List<String>> rows = new ArrayList<>();

            String line = reader.readLine();
            if (line != null) {
                // First line contains column names
                columnNames = Arrays.asList(line.split("\t")); // Assuming columns are tab-separated
            }

            // Read the rest of the file for rows
            while ((line = reader.readLine()) != null) {
                List<String> row = Arrays.asList(line.split("\t")); // Assuming rows are tab-separated
                rows.add(row);
            }

            // Build the table object
            Table table = new Table(columnNames);
            for (List<String> row : rows) {
                table.addRow(row);
            }

            return table;
        } catch (IOException e) {
            System.err.println("Error loading table " + tableName + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }

    }

    //public String selectColumnsWithCondition(List<String> columnNames, Condition condition) {

    //}
}