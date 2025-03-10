package edu.uob;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.stream.Collectors;
import java.util.Iterator;

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
                String regex = conditionValue.replace("%", ".*").replace("_", ".");
                return rowValue.matches(regex); // Simple "contains" logic for LIKE
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

    public int updateRowsWithConditions(List<String> columnsToUpdate, List<String> newValues, List<String> conditions) throws Exception {
        int updateCount = 0;

        for (List<String> row : rows) {
            // Check if the row matches the conditions
            if (isRowMatchConditions(row, conditions, columns)) {
                // Update each specified column
                for (int i = 0; i < columnsToUpdate.size(); i++) {
                    int colIndex = columns.indexOf(columnsToUpdate.get(i));
                    if (colIndex != -1) {
                        row.set(colIndex, newValues.get(i));
                    }
                }
                updateCount++; // Increment the counter
            }
        }

        return updateCount;
    }

    public List<List<String>> joinWith(Table other, String column1, String column2) {
        int column1Index = this.columns.indexOf(column1);
        int column2Index = other.getColumns().indexOf(column2);

        List<List<String>> result = new ArrayList<>();

        for (List<String> row1 : rows) {
            for (List<String> row2 : other.getRows()) {
                if (row1.get(column1Index).equals(row2.get(column2Index))) {
                    // Combine the rows
                    List<String> combinedRow = new ArrayList<>(row1);
                    combinedRow.addAll(row2);
                    result.add(combinedRow);
                }
            }
        }

        return result;
    }

    public int deleteRowsWithConditions(List<String> conditions) throws Exception {
        int deleteCount = 0;

        // Use an iterator to avoid ConcurrentModificationException
        Iterator<List<String>> iter = rows.iterator();
        while (iter.hasNext()) {
            List<String> row = iter.next();

            // Check if row matches ALL conditions
            if (isRowMatchConditions(row, conditions, columns)) { // Use the existing method
                iter.remove(); // Remove the row
                deleteCount++;
            }
        }

        return deleteCount;
    }


    //public String selectColumnsWithCondition(List<String> columnNames, Condition condition) {

    //}
}






//package edu.uob;
//
//import java.io.*;
//import java.util.*;
//
//public class Table {
//    private List<String> columns;
//    private List<List<String>> rows;
//
//    public Table(List<String> columns) {
//        if (columns == null) {
//            this.columns = new ArrayList<>();
//            if (!this.columns.contains("id")) {
//                this.columns.add("id");
//            }
//        } else {
//            this.columns = new ArrayList<>(columns);
//            if (!this.columns.contains("id")) {
//                this.columns.add(0, "id");
//            }
//        }
//        this.rows = new ArrayList<>();
//    }
//
//    public void addRow(List<String> values) {
//        // Ensure row has the correct number of columns
//        if (values.size() != columns.size()) {
//            throw new IllegalArgumentException("Row size must match column size");
//        }
//        rows.add(new ArrayList<>(values));
//    }
//
//    public List<String> getColumns() {
//        return new ArrayList<>(columns);
//    }
//
//    public boolean addColumn(String columnName) {
//        if (columns.contains(columnName)) {
//            return false;
//        }
//        columns.add(columnName);
//        // Add empty values for this column in all existing rows
//        for (List<String> row : rows) {
//            row.add("NULL");
//        }
//        return true;
//    }
//
//    public boolean dropColumn(String columnName) {
//        int columnIndex = columns.indexOf(columnName);
//        if (columnIndex == -1) {
//            return false;
//        }
//        if (columnName.equalsIgnoreCase("id")) {
//            return false;
//        }
//        columns.remove(columnIndex);
//        // Remove this column from all rows
//        for (List<String> row : rows) {
//            row.remove(columnIndex);
//        }
//        return true;
//    }
//
//    // Save table to a file
//    public void saveToFile(String directoryPath, String tableName) throws IOException {
//        File file = new File(directoryPath, tableName + DBServer.FILE_EXTENSION);
//        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
//            // Write column headers
//            writer.write(String.join("\t", columns));
//            writer.newLine();
//
//            // Write data rows
//            for (List<String> row : rows) {
//                writer.write(String.join("\t", row));
//                writer.newLine();
//            }
//        }
//    }
//
//    // Load table from a file
//    public Table loadFromFile(String directoryPath, String tableName) throws IOException {
//        File file = new File(directoryPath, tableName + DBServer.FILE_EXTENSION);
//        if (!file.exists()) {
//            return null;
//        }
//
//        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
//            // Read column headers
//            String headerLine = reader.readLine();
//            if (headerLine == null) {
//                return null;
//            }
//
//            List<String> loadedColumns = Arrays.asList(headerLine.split("\t"));
//            Table table = new Table(loadedColumns);
//
//            // Read data rows
//            String line;
//            while ((line = reader.readLine()) != null) {
//                String[] values = line.split("\t");
//                // If there are fewer values than columns, pad with NULLs
//                List<String> rowValues = new ArrayList<>(Arrays.asList(values));
//                while (rowValues.size() < loadedColumns.size()) {
//                    rowValues.add("NULL");
//                }
//                table.rows.add(rowValues);
//            }
//
//            return table;
//        }
//    }
//
//    // Select rows without conditions (return all rows)
//    public List<List<String>> selectRowsWithoutConditions(List<String> selectedColumns) {
//        List<List<String>> result = new ArrayList<>();
//        List<Integer> columnIndices = getColumnIndices(selectedColumns);
//
//        for (List<String> row : rows) {
//            List<String> selectedRow = new ArrayList<>();
//            for (int index : columnIndices) {
//                selectedRow.add(row.get(index));
//            }
//            result.add(selectedRow);
//        }
//
//        return result;
//    }
//
//    // Select rows with conditions
//    public List<List<String>> selectRowsWithConditions(List<String> selectedColumns, List<String> conditions) {
//        List<List<String>> result = new ArrayList<>();
//        List<Integer> columnIndices = getColumnIndices(selectedColumns);
//
//        for (List<String> row : rows) {
//            if (matchesConditions(row, conditions)) {
//                List<String> selectedRow = new ArrayList<>();
//                for (int index : columnIndices) {
//                    selectedRow.add(row.get(index));
//                }
//                result.add(selectedRow);
//            }
//        }
//
//        return result;
//    }
//
//    // Update rows that match conditions
//    public int updateRowsWithConditions(List<String> updateColumns, List<String> updateValues, List<String> conditions) {
//        int updatedCount = 0;
//        List<Integer> columnIndices = getColumnIndices(updateColumns);
//
//        for (List<String> row : rows) {
//            if (matchesConditions(row, conditions)) {
//                for (int i = 0; i < columnIndices.size(); i++) {
//                    row.set(columnIndices.get(i), updateValues.get(i));
//                }
//                updatedCount++;
//            }
//        }
//
//        return updatedCount;
//    }
//
//    // Delete rows that match conditions
//    public int deleteRowsWithConditions(List<String> conditions) {
//        int initialSize = rows.size();
//        rows.removeIf(row -> matchesConditions(row, conditions));
//        return initialSize - rows.size();
//    }
//
//    // Helper method to check if a row matches conditions
//    private boolean matchesConditions(List<String> row, List<String> conditions) {
//        if (conditions == null || conditions.isEmpty()) {
//            return true;
//        }
//
//        // Process each condition
//        for (String condition : conditions) {
//            // Parse the condition
//            String[] parts = parseCondition(condition);
//            if (parts == null || parts.length < 3) {
//                continue;
//            }
//
//            String columnName = parts[0].trim();
//            String operator = parts[1].trim();
//            String value = parts[2].trim();
//
//            // Get the column index
//            int columnIndex = columns.indexOf(columnName);
//            if (columnIndex == -1) {
//                return false;
//            }
//
//            // Get the actual value from the row
//            String rowValue = row.get(columnIndex);
//
//            // Remove quotes from value if present
//            if ((value.startsWith("'") && value.endsWith("'")) ||
//                    (value.startsWith("\"") && value.endsWith("\""))) {
//                value = value.substring(1, value.length() - 1);
//            }
//
//            // Check the condition
//            if (!evaluateCondition(rowValue, operator, value)) {
//                return false;
//            }
//        }
//
//        return true;
//    }
//
//    // Helper method to parse a condition string into components
//    private String[] parseCondition(String condition) {
//        String[] operators = {"==", "!=", ">=", "<=", ">", "<", "LIKE"};
//
//        for (String op : operators) {
//            int index = condition.indexOf(op);
//            if (index > 0) {
//                String column = condition.substring(0, index).trim();
//                String value = condition.substring(index + op.length()).trim();
//                return new String[]{column, op, value};
//            }
//        }
//
//        // If no recognized operator, try with =
//        int index = condition.indexOf("=");
//        if (index > 0) {
//            String column = condition.substring(0, index).trim();
//            String value = condition.substring(index + 1).trim();
//            return new String[]{column, "==", value};
//        }
//
//        return null;
//    }
//
//    // Helper method to evaluate a single condition
//    private boolean evaluateCondition(String rowValue, String operator, String targetValue) {
//        // Handle null values
//        if (rowValue.equals("NULL") || targetValue.equals("NULL")) {
//            if (operator.equals("==")) return rowValue.equals(targetValue);
//            if (operator.equals("!=")) return !rowValue.equals(targetValue);
//            return false; // Other comparisons with NULL are false
//        }
//
//        // Try numeric comparison if both values are numeric
//        try {
//            double rowNum = Double.parseDouble(rowValue);
//            double targetNum = Double.parseDouble(targetValue);
//
//            switch (operator) {
//                case "==": return rowNum == targetNum;
//                case "!=": return rowNum != targetNum;
//                case ">": return rowNum > targetNum;
//                case "<": return rowNum < targetNum;
//                case ">=": return rowNum >= targetNum;
//                case "<=": return rowNum <= targetNum;
//                default: return false;
//            }
//        } catch (NumberFormatException e) {
//            // If not numeric, do string comparison
//            switch (operator) {
//                case "==": return rowValue.equals(targetValue);
//                case "!=": return !rowValue.equals(targetValue);
//                case ">": return rowValue.compareTo(targetValue) > 0;
//                case "<": return rowValue.compareTo(targetValue) < 0;
//                case ">=": return rowValue.compareTo(targetValue) >= 0;
//                case "<=": return rowValue.compareTo(targetValue) <= 0;
//                case "LIKE": return rowValue.contains(targetValue);
//                default: return false;
//            }
//        }
//    }
//
//    // Helper method to get column indices for selected columns
//    private List<Integer> getColumnIndices(List<String> selectedColumns) {
//        List<Integer> indices = new ArrayList<>();
//
//        // If '*' is included, return all columns
//        if (selectedColumns.contains("*")) {
//            for (int i = 0; i < columns.size(); i++) {
//                indices.add(i);
//            }
//            return indices;
//        }
//
//        // Otherwise, get indices for the specified columns
//        for (String column : selectedColumns) {
//            int index = columns.indexOf(column);
//            if (index != -1) {
//                indices.add(index);
//            }
//        }
//
//        return indices;
//    }
//
//    // Generate a new unique ID for inserting rows
//    public int getNextId() {
//        int maxId = 0;
//        int idIndex = columns.indexOf("id");
//
//        if (idIndex != -1) {
//            for (List<String> row : rows) {
//                try {
//                    int id = Integer.parseInt(row.get(idIndex));
//                    if (id > maxId) {
//                        maxId = id;
//                    }
//                } catch (NumberFormatException e) {
//                    // Skip non-numeric IDs
//                }
//            }
//        }
//
//        return maxId + 1;
//    }
//
//    // Insert a new row with values
//    public boolean insertRow(List<String> values) {
//        // Ensure we have correct number of values
//        if (values.size() != columns.size() - 1) { // -1 because we'll add ID
//            return false;
//        }
//
//        // Create a new row with ID
//        List<String> newRow = new ArrayList<>();
//        newRow.add(String.valueOf(getNextId())); // Add ID as first column
//        newRow.addAll(values);
//
//        // Add the row
//        rows.add(newRow);
//        return true;
//    }
//
//    public List<List<String>> joinWith(Table other, String column1, String column2) {
//        int column1Index = this.columns.indexOf(column1);
//        int column2Index = other.getColumns().indexOf(column2);
//
//        List<List<String>> result = new ArrayList<>();
//
//        for (List<String> row1 : rows) {
//            for (List<String> row2 : other.getRows()) {
//                if (row1.get(column1Index).equals(row2.get(column2Index))) {
//                    // Combine the rows
//                    List<String> combinedRow = new ArrayList<>(row1);
//                    combinedRow.addAll(row2);
//                    result.add(combinedRow);
//                }
//            }
//        }
//
//        return result;
//    }
//
//    public List<List<String>> getRows() {
//        return rows;
//    }
//}
