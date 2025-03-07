package edu.uob;

import java.io.*;
import java.util.ArrayList;
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
            row.add("");
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

    public String selectAllColumns(List<String> conditions) {
        StringBuilder result = new StringBuilder();
        result.append(String.join(" | ", columns)).append("\n");

        for (List<String> row : rows) {
            if (isRowMatchConditions(row, conditions)) {
                result.append(String.join(" | ", row)).append("\n");
            }
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

    public String selectColumns(List<String> selectedColumns, List<String> conditions) {
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

        return result.toString();
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
        for (String condition : conditions) {
            // Use regex to split by operators
            List<String> parts = Arrays.asList(condition.split("(==|!=|>=|<=|<|>| LIKE )"));
            if (parts.size() != 3) {
                throw new Exception("[ERROR] Invalid WHERE clause format in '" + condition + "'.");
            }

            String column = parts.get(1).trim();
            String operator = parts.get(2).trim();
            String value = parts.get(3).trim();

            int columnIndex = columns.indexOf(column);
            if (columnIndex == -1) {
                throw new Exception("[ERROR] Column '" + column + "' does not exist.");
            }

            // Now that we have the column value from the row, compare it with the value from the condition
            String rowValue = row.get(columnIndex).trim();

            // Check if the operator is LIKE or not
            if (operator.equals("LIKE")) {
                // For LIKE operator, we consider that '%' can be at the beginning/end/both/nor of the value
                // Please be aware, this is very basic implementation and doesn't cover all SQL LIKE operator features
                if (value.startsWith("%") && value.endsWith("%")) {
                    if (!rowValue.contains(value.substring(1, value.length() - 1))) {
                        return false;
                    }
                } else if (value.startsWith("%")) {
                    if (!rowValue.endsWith(value.substring(1))) {
                        return false;
                    }
                } else if (value.endsWith("%")) {
                    if (!rowValue.startsWith(value.substring(0, value.length() - 1))) {
                        return false;
                    }
                } else {
                    if (!rowValue.equals(value)) {
                        return false;
                    }
                }
            } else {
                // For non-LIKE operators, compare the numerical values
                double rowValueNum;
                double valueNum;
                try {
                    rowValueNum = Double.parseDouble(rowValue);
                    valueNum = Double.parseDouble(value);
                } catch (NumberFormatException e) {
                    throw new Exception("[ERROR] Invalid comparison between non-numeric values in condition '" + condition + "'.");
                }

                switch (operator) {
                    case "==":
                        if (rowValueNum != valueNum) {
                            return false;
                        }
                        break;
                    case "!=":
                        if (rowValueNum == valueNum) {
                            return false;
                        }
                        break;
                    case ">":
                        if (rowValueNum <= valueNum) {
                            return false;
                        }
                        break;
                    case "<":
                        if (rowValueNum >= valueNum) {
                            return false;
                        }
                        break;
                    case ">=":
                        if (rowValueNum < valueNum) {
                            return false;
                        }
                        break;
                    case "<=":
                        if (rowValueNum > valueNum) {
                            return false;
                        }
                        break;
                    default:
                        throw new Exception("[ERROR] Invalid operator '"+ operator +"' in condition '" + condition + "'.");
                }
            }
        }
        return true;  // If none of the conditions failed, this row matches all conditions
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


    public static Table loadFromFile(String databasePath, String tableName) {
        File tableFile = new File(databasePath, tableName + FILE_EXTENSION);
        if (!tableFile.exists()) {
            return null;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(tableFile))) {
            return (Table) ois.readObject(); // Deserialize table data
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading table " + tableName + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    //public String selectColumnsWithCondition(List<String> columnNames, Condition condition) {

    //}
}