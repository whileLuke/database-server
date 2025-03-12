package edu.uob;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class DBStorage {
    private final String baseStoragePath;

    public DBStorage(String baseStoragePath) {
        this.baseStoragePath = baseStoragePath;
        ensureStorageDirectoryExists();
    }

    private void ensureStorageDirectoryExists() {
        try {
            Files.createDirectories(Paths.get(baseStoragePath));
        } catch (IOException e) {
        }
    }

    public boolean createDatabase(String dbName) {
        if (dbName == null || dbName.isEmpty()) return false;

        File dbDirectory = getDatabaseDirectory(dbName);
        if (dbDirectory.exists()) return false;

        return dbDirectory.mkdirs();
    }

    public boolean databaseExists(String dbName) {
        if (dbName == null || dbName.isEmpty()) return false;

        File dbDirectory = getDatabaseDirectory(dbName);
        return dbDirectory.exists() && dbDirectory.isDirectory();
    }

    public boolean deleteDatabase(String dbName) {
        File dbDirectory = getDatabaseDirectory(dbName);
        if (!dbDirectory.exists() || !dbDirectory.isDirectory()) return false;

        return deleteDirectory(dbDirectory);
    }

    private boolean deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    if (!deleteDirectory(file)) return false;
                } else if (!file.delete()) return false;
            }
        }
        return directory.delete();
    }

    public Map<String, DBTable> loadTables(String dbName) throws IOException {
        File dbDirectory = getDatabaseDirectory(dbName);
        if (!dbDirectory.exists() || !dbDirectory.isDirectory()) {
            throw new IOException("Database directory does not exist: " + dbName);
        }

        Map<String, DBTable> tables = new HashMap<>();
        File[] tableFiles = dbDirectory.listFiles((dir, name) -> name.endsWith(".tab"));

        if (tableFiles != null) {
            for (File tableFile : tableFiles) {
                String tableName = getTableNameFromFile(tableFile);
                DBTable table = loadTableFromFile(dbName, tableName);
                if (table != null) {
                    tables.put(tableName.toLowerCase(), table);
                }
            }
        }

        return tables;
    }

    public boolean saveTable(DBTable table, String dbName) throws IOException {
        if (table == null || dbName == null || dbName.isEmpty()) return false;

        File dbDirectory = getDatabaseDirectory(dbName);
        if (!dbDirectory.exists()) {
            if (!dbDirectory.mkdirs()) {
                throw new IOException("Failed to create database directory: " + dbName);
            }
        }

        File tableFile = getTableFile(dbName, table.getName());
        return writeTableToFile(table, tableFile);
    }

    public boolean saveTables(Map<String, DBTable> tables, String dbName) throws IOException {
        if (tables == null || dbName == null || dbName.isEmpty()) return false;

        boolean allSaved = true;
        for (DBTable table : tables.values()) {
            if (!saveTable(table, dbName)) {
                allSaved = false;
            }
        }

        return allSaved;
    }

    private DBTable loadTableFromFile(String dbName, String tableName) throws IOException {
        File tableFile = getTableFile(dbName, tableName);
        if (!tableFile.exists()) return null;

        try (BufferedReader reader = new BufferedReader(new FileReader(tableFile))) {
            // Read headers
            String headerLine = reader.readLine();
            if (headerLine == null) return null;

            List<String> columns = new ArrayList<>(Arrays.asList(headerLine.split("\t")));
            DBTable table = new DBTable(tableName, columns);

            // Read data rows
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isEmpty()) {
                    List<String> rowData = new ArrayList<>(Arrays.asList(line.split("\t")));
                    table.addRowDirect(rowData);
                }
            }

            return table;
        }
    }

    private boolean writeTableToFile(DBTable table, File tableFile) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tableFile))) {
            // Write headers
            writer.write(String.join("\t", table.getColumns()));
            writer.newLine();

            // Write data rows
            for (List<String> row : table.getRows()) {
                writer.write(String.join("\t", row));
                writer.newLine();
            }

            return true;
        }
    }

    private File getDatabaseDirectory(String dbName) {
        return new File(baseStoragePath, dbName.toLowerCase());
    }

    private File getTableFile(String dbName, String tableName) {
        return new File(getDatabaseDirectory(dbName), tableName.toLowerCase() + ".tab");
    }

    private String getTableNameFromFile(File tableFile) {
        String fileName = tableFile.getName();
        return fileName.substring(0, fileName.lastIndexOf("."));
    }
}
