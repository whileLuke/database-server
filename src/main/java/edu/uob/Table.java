package edu.uob;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Table implements Serializable {
    private final List<String> columns;
    private final List<List<String>> rows;
    private final String name;

    public Table(String name, List<String> columns) {
        this.name = name;
        if (columns != null) this.columns = new ArrayList<>(columns);
        else this.columns = new ArrayList<>();
        this.rows = new ArrayList<>();
    }

    public String getName() { return name; }

    public List<String> getColumns() { return new ArrayList<>(columns); }

    public List<List<String>> getRows() { return rows; }

    public int getColumnIndex(String columnName) {
        return columns.indexOf(columnName);
    }
    
    public boolean addRow(List<String> row) {
        if (row.size() != columns.size()) {
            return false;
        }
        rows.add(new ArrayList<>(row));
        return true;
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

    @Override
    public String toString() {
        return TableFormatter.formatTable(this);
    }
}
