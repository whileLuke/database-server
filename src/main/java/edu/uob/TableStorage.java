package edu.uob;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TableStorage {
    private static final String FILE_EXTENSION = ".tab";

    public static void saveToFile(Table table, String databasePath) throws IOException {
        File tableFile = new File(databasePath, table.getName() + FILE_EXTENSION);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tableFile))) {
            // Write columns
            writer.write(String.join("\t", table.getColumns()));
            writer.newLine();

            // Write rows
            for (List<String> row : table.getRows()) {
                writer.write(String.join("\t", row));
                writer.newLine();
            }
        }
    }

    public static Table loadFromFile(String databasePath, String tableName) throws IOException {
        File tableFile = new File(databasePath, tableName + FILE_EXTENSION);
        if (!tableFile.exists()) {
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(tableFile))) {
            String line = reader.readLine();
            if (line == null) {
                throw new IOException("Empty table file: " + tableFile.getPath());
            }

            // Parse column names
            List<String> columnNames = Arrays.asList(line.split("\t"));
            Table table = new Table(tableName, columnNames);

            // Parse rows
            while ((line = reader.readLine()) != null) {
                String[] values = line.split("\t");

                // Handle case where row might have fewer columns than expected
                List<String> rowValues = new ArrayList<>(Arrays.asList(values));
                while (rowValues.size() < columnNames.size()) {
                    rowValues.add("NULL");
                }
                table.addRow(rowValues);
            }

            return table;
        }
    }
}