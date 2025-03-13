package edu.uob;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

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
        for (List<String> row : rows) row.add(null);
        return true;
    }

    public boolean dropColumn(String columnName) {
        int columnIndex = columns.indexOf(columnName);
        if (columnIndex == -1) return false;
        columns.remove(columnIndex);
        for (List<String> row : rows) row.remove(columnIndex);
        return true;
    }

    public int generateNextID() throws IOException {
        addIdColumnIfNotExists();
        int maxID = Math.max(getMaxIDFromTable(), readMaxIDFromFile());
        int newID = maxID + 1;
        writeNewMaxID(newID);
        return newID;
    }

    private void addIdColumnIfNotExists() {
        int idColumnIndex = columns.indexOf("id");
        if (idColumnIndex == -1) {
            columns.add(0, "id");
            for (List<String> row : rows) row.add(0, null);
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

    private int readMaxIDFromFile() throws IOException {
        int maxID = 0;
        File maxIDFile = getMaxIDFile();
        if (maxIDFile.exists()) {
            BufferedReader reader = new BufferedReader(new FileReader(maxIDFile));
            String maxIDInFile = reader.readLine();
            if (maxIDInFile != null) maxID = Integer.parseInt(maxIDInFile.trim());
        }
        return maxID;
    }

    private File getMaxIDFile() {
        return new File(DBServer.storageFolderPath + File.separator +
                DBServer.currentDB + File.separator + tableName + ".maxid");
    }

    private void writeNewMaxID(int newID) throws IOException {
        File maxIDFile = getMaxIDFile();
        BufferedWriter writer = new BufferedWriter(new FileWriter(maxIDFile));
        writer.write(String.valueOf(newID));
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
