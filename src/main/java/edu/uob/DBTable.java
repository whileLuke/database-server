package edu.uob;

import java.util.ArrayList;
import java.util.List;
import java.io.*;

public class DBTable {
    private final List<String> columns;
    private final List<List<String>> rows;
    private final String tableName;

    public DBTable(String tableName, List<String> columns) {
        this.tableName = tableName;
        if (columns != null) this.columns = new ArrayList<>(columns);
        else this.columns = new ArrayList<>();
        this.rows = new ArrayList<>();
    }

    public String getName() { return tableName; }

    public List<String> getColumns() { return new ArrayList<>(columns); }

    public List<List<String>> getRows() { return rows; }

    public int getColumnIndex(String columnName) {
        return columns.indexOf(columnName);
    }

    public boolean addRow(List<String> row) {
        if (row.size() != columns.size()) return false;
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
        for (List<String> row : rows) row.remove(columnIndex);
        return true;
    }

    public int generateNextID() {
        addIdColumnIfNotExists();
        int maxIDInRows = getMaxIDFromTable();
        int maxIDFromFile = readMaxIDFromFile();

        int maxID = Math.max(maxIDInRows, maxIDFromFile);
        int newID = maxID + 1;

        saveNewMaxID(newID);
        return newID;
    }

    private void addIdColumnIfNotExists() {
        int idColumnIndex = columns.indexOf("id");
        if (idColumnIndex == -1) {
            columns.add(0, "id");
            for (List<String> row : rows) row.add(0, "0");
        }
    }

    private int getMaxIDFromTable() {
        int idColumnIndex = columns.indexOf("id");
        int maxID = 0;
        for (List<String> row : rows) {
            int currentID = Integer.parseInt(row.get(idColumnIndex));
            if (currentID > maxID) maxID = currentID;
        }
        return maxID;
    }

    private int readMaxIDFromFile() {
        int maxID = 0;
        File maxIDFile = getMaxIDFile();
        if (maxIDFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(maxIDFile))) {
                String line = reader.readLine();
                if (line != null) {
                    maxID = Integer.parseInt(line.trim());
                }
            } catch (IOException ignored) {}
        }
        return maxID;
    }

    private File getMaxIDFile() {
        return new File(DBServer.storageFolderPath + File.separator +
                DBServer.currentDB + File.separator +
                tableName + ".maxid");
    }

    private void saveNewMaxID(int newID) {
        File maxIDFile = getMaxIDFile();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(maxIDFile))) {
            writer.write(String.valueOf(newID));
        } catch (IOException e) {
            // Kept your original error message
            System.err.println("Could not write to the maxID file" + e.getMessage());
        }
    }

    @Override
    public String toString() {
        StringBuilder tableString = new StringBuilder();
        tableString.append(String.join("\t", columns)).append("\n");
        for (List<String> row : rows) tableString.append(String.join("\t", row)).append("\n");
        if (!tableString.isEmpty()) tableString.setLength(tableString.length() - 1);
        return tableString.toString();
    }
}
