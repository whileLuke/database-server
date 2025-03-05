package edu.uob;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Table implements Serializable {
    //private static final long serialVersionUID = 1L;
    private final List<String> columns;
    private final List<List<String>> rows;

    public Table(List<String> columns) {
        this.columns = columns;
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
        rows.add(new ArrayList<>(values));
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
        return true; // Rows updated successfully
    }

    public int generateNextID() {
        int idColumnIndex = columns.indexOf("id");
        if (idColumnIndex == -1) throw new IllegalStateException("The table does not have an 'ID' column.");
        int maxID = 0;
        for (List<String> row : rows) {
            try {
                int currentID = Integer.parseInt(row.get(idColumnIndex));
                if (currentID > maxID) maxID = currentID;
            } catch (NumberFormatException ignored) {
            }
        }
        return maxID + 1;
    }

    public void saveToFile(String databasePath, String tableName) {
        File tableFile = new File(databasePath, tableName + ".tab");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(tableFile))) {
            oos.writeObject(this); // Serialize table data
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Table loadFromFile(String databasePath, String tableName) {
        File tableFile = new File(databasePath, tableName + ".tab");
        if (!tableFile.exists()) {
            return null;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(tableFile))) {
            return (Table) ois.readObject(); // Deserialize table data
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}