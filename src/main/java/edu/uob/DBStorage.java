package edu.uob;

import java.io.*;
import java.util.*;

public class DBStorage {
    private final String baseStoragePath;

    public DBStorage(String baseStoragePath) { this.baseStoragePath = baseStoragePath; }

    public boolean createDB(String DBName) {
        if (DBName == null || DBName.isEmpty()) return false;
        File dbDirectory = getDBDirectory(DBName);
        if (dbDirectory.exists()) return false;
        return dbDirectory.mkdirs();
    }

    public boolean DBExists(String DBName) {
        if (DBName == null || DBName.isEmpty()) return false;

        File dbDirectory = getDBDirectory(DBName);
        return dbDirectory.exists() && dbDirectory.isDirectory();
    }

    public boolean deleteDB(String DBName) {
        File DBDirectory = getDBDirectory(DBName);
        if (!DBDirectory.exists() || !DBDirectory.isDirectory()) return false;

        return deleteDirectory(DBDirectory);
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

    public Map<String, DBTable> loadTables(String DBName) throws IOException {
        File dbDirectory = getDBDirectory(DBName);
        if (!dbDirectory.exists() || !dbDirectory.isDirectory()) {
            throw new IOException("Database directory does not exist: " + DBName);
        }

        Map<String, DBTable> tables = new HashMap<>();
        File[] tableFiles = dbDirectory.listFiles((dir, name) -> name.endsWith(".tab"));

        if (tableFiles != null) {
            for (File tableFile : tableFiles) {
                String tableName = getTableName(tableFile);
                DBTable table = loadTableFromFile(DBName, tableName);
                if (table != null) {
                    tables.put(tableName.toLowerCase(), table);
                }
            }
        }

        return tables;
    }

    public boolean saveTable(DBTable table, String DBName) throws IOException {
        if (table == null || DBName == null || DBName.isEmpty()) return false;

        File DBDirectory = getDBDirectory(DBName);
        if (!DBDirectory.exists()) {
            if (!DBDirectory.mkdirs()) {
                throw new IOException("Failed to create database directory: " + DBName);
            }
        }

        File tableFile = getTableFile(DBName, table.getName());
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

    private DBTable loadTableFromFile(String DBName, String tableName) throws IOException {
        File tableFile = getTableFile(DBName, tableName);
        if (!tableFile.exists()) return null;

        BufferedReader reader = new BufferedReader(new FileReader(tableFile));
        String headerLine = reader.readLine();
        if (headerLine == null) return null;

        List<String> columns = new ArrayList<>(Arrays.asList(headerLine.split("\t")));
        DBTable table = new DBTable(tableName, columns);
        String line;

        while ((line = reader.readLine()) != null) {
            if (!line.isEmpty()) {
                List<String> rowData = new ArrayList<>(Arrays.asList(line.split("\t")));
                table.addRow(rowData);
            }
        }
        return table;
    }

    private boolean writeTableToFile(DBTable table, File tableFile) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tableFile))) {
            writer.write(String.join("\t", table.getColumns()));
            writer.newLine();

            for (List<String> row : table.getRows()) {
                writer.write(String.join("\t", row));
                writer.newLine();
            }
            return true;
        }
    }

    private File getDBDirectory(String dbName) {
        return new File(baseStoragePath, dbName.toLowerCase());
    }

    private File getTableFile(String dbName, String tableName) {
        return new File(getDBDirectory(dbName), tableName.toLowerCase() + ".tab");
    }

    private String getTableName(File tableFile) {
        String fileName = tableFile.getName();
        return fileName.substring(0, fileName.lastIndexOf("."));
    }
}
