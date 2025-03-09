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
            if (isRowMatchConditions(row, conditions)) {
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
        List<Integer> columnIndexes = getColumnIndexes(selectedColumns); // Get indexes of the selected columns

        for (List<String> row : rows) {
            if (isRowMatchConditions(row, conditions)) {
                // Add only the values in the requested columns
                filteredRows.add(
                        columnIndexes.stream()
                                .map(row::get)
                                .collect(Collectors.toList())
                );
            }
        }
        return filteredRows;
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
            if (isRowMatchConditions(row, conditions)) {
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

    private boolean isRowMatchConditions(List<String> row, List<String> conditions) throws Exception {
        boolean result = evaluateCondition(row, conditions.get(0)); // First condition
        for (int i = 1; i < conditions.size(); i += 2) {
            String logicalOperator = conditions.get(i); // AND / OR
            boolean nextConditionResult = evaluateCondition(row, conditions.get(i + 1));
            if (logicalOperator.equalsIgnoreCase("AND")) {
                result = result && nextConditionResult;
            } else if (logicalOperator.equalsIgnoreCase("OR")) {
                result = result || nextConditionResult;
            } else {
                throw new Exception("[ERROR] Invalid logical operator: " + logicalOperator);
            }
        }
        return result;
    }

    private boolean evaluateCondition(List<String> row, String condition) {
        // Parse condition (e.g., "columnName = value")
        String[] conditionParts = condition.split("\\s+");
        if (conditionParts.length != 3) return false;
        String columnName = conditionParts[0];
        String operator = conditionParts[1];
        String value = conditionParts[2];

        int columnIndex = columns.indexOf(columnName);
        if (columnIndex == -1) return false; // Column doesn't exist
        String cellValue = row.get(columnIndex);

        // Evaluate based on operator
        return switch (operator) {
            case "=" -> cellValue.equals(value);
            case "!=" -> !cellValue.equals(value);
            case ">" -> Double.parseDouble(cellValue) > Double.parseDouble(value);
            case ">=" -> Double.parseDouble(cellValue) >= Double.parseDouble(value);
            case "<" -> Double.parseDouble(cellValue) < Double.parseDouble(value);
            case "<=" -> Double.parseDouble(cellValue) <= Double.parseDouble(value);
            case "LIKE" -> cellValue.contains(value);
            default -> false; // Invalid operator
        };
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