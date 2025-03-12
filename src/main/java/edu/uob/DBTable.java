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
        //this.server = server;
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

    /*public int generateNextID() {
        int idColumnIndex = columns.indexOf("id");
        if (idColumnIndex == -1) {
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
            } catch (NumberFormatException ignored) {}
        }
        //String currentDirectoryPath = server.storageFolderPath + File.separator + server.getCurrentDB();
        String databaseDir = new File(DBServer.storageFolderPath, DBServer.currentDB).getAbsolutePath();
        File maxIDFile = new File(databaseDir, tableName + ".maxid");
        //File maxIDFile = new File(currentDirectoryPath + File.separator + tableName + ".maxid");
        if (maxIDFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(maxIDFile))) {
                int storedMaxID = Integer.parseInt(reader.readLine());
                if (storedMaxID > maxID) maxID = storedMaxID;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (maxIDFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(maxIDFile))) {
                String line = reader.readLine();
                if (line != null && !line.isEmpty()) {
                    try {
                        int storedMaxID = Integer.parseInt(line.trim());
                        maxID = Math.max(maxID, storedMaxID);
                    } catch (NumberFormatException ignored) {
                        // Ignore parsing errors
                    }
                }
            } catch (IOException e) {
                System.err.println("Error reading max ID file: " + e.getMessage());
                // Continue with the maxID from the table
            }
        }

        int newID = maxID + 1;
        File dir = maxIDFile.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(maxIDFile))) {
            writer.write(String.valueOf(newID));
        } catch (IOException e) {
            System.err.println("Could not write to the maxID file" + e.getMessage());
        }
        return newID;
    }*/

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
        String tableDataDir = DBServer.storageFolderPath + File.separator + DBServer.currentDB + File.separator + tableName;
        File metaFile = new File(tableDataDir + ".meta");

        int maxID = 0;

        // First, find the current max ID in the table as a fallback
        for (List<String> row : rows) {
            try {
                int currentID = Integer.parseInt(row.get(idColumnIndex));
                if (currentID > maxID) maxID = currentID;
            } catch (NumberFormatException ignored) {
                // Skip non-numeric values
            }
        }

        // If the meta file exists, read the highest ID from it
        if (metaFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(metaFile))) {
                String line = reader.readLine();
                if (line != null && !line.isEmpty()) {
                    try {
                        int storedMaxID = Integer.parseInt(line.trim());
                        // Use the higher of the two values
                        maxID = Math.max(maxID, storedMaxID);
                    } catch (NumberFormatException ignored) {
                        // If parsing fails, continue with the maxID from the table
                    }
                }
            } catch (IOException e) {
                System.err.println("Error reading max ID from meta file: " + e.getMessage());
            }
        }

        // Increment the max ID
        int nextID = maxID + 1;

        // Save the new max ID back to the meta file
        try {
            // Ensure the directory exists
            File dir = new File(tableDataDir).getParentFile();
            if (dir != null && !dir.exists()) {
                dir.mkdirs();
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(metaFile))) {
                writer.write(String.valueOf(nextID));
            }
        } catch (IOException e) {
            System.err.println("Error writing max ID to meta file: " + e.getMessage());
        }

        return nextID;
    }

    public void addRowDirect(List<String> row) {
        rows.add(new ArrayList<>(row));
    }

    // Add a method to find rows by column value
    public List<List<String>> findRowsByColumnValue(String columnName, String value) {
        int columnIndex = getColumnIndex(columnName);
        if (columnIndex == -1) return new ArrayList<>();

        List<List<String>> matchingRows = new ArrayList<>();
        for (List<String> row : rows) {
            if (row.get(columnIndex).equals(value)) {
                matchingRows.add(row);
            }
        }
        return matchingRows;
    }

    @Override
    public String toString() {
        return formatRows(columns, rows);
    }

    public static String formatRows(List<String> columns, List<List<String>> rows) {
        StringBuilder result = new StringBuilder();
        result.append(String.join("\t", columns)).append("\n");
        for (List<String> row : rows) result.append(String.join("\t", row)).append("\n");
        if (!result.isEmpty()) result.setLength(result.length() - 1);
        return result.toString();
    }
}
