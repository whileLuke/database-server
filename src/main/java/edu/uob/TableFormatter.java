package edu.uob;

import java.util.List;

public class TableFormatter {

    public static String formatTable(Table table) {
        return formatRows(table.getColumns(), table.getRows());
    }

    public static String formatRows(List<String> columns, List<List<String>> rows) {
        StringBuilder result = new StringBuilder();
        result.append(String.join("\t", columns)).append("\n");
        for (List<String> row : rows) {
            result.append(String.join("\t", row)).append("\n");
        }
        if (result.length() > 0) {
            result.setLength(result.length() - 1);
        }
        return result.toString();
    }

    public static String formatTablePretty(Table table) {
        List<String> columns = table.getColumns();
        List<List<String>> rows = table.getRows();

        // Calculate column widths
        int[] columnWidths = new int[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            columnWidths[i] = columns.get(i).length();
            for (List<String> row : rows) {
                columnWidths[i] = Math.max(columnWidths[i], row.get(i).length());
            }
        }

        // Build pretty table
        StringBuilder result = new StringBuilder();

        // Header
        for (int i = 0; i < columns.size(); i++) {
            result.append(String.format("%-" + (columnWidths[i] + 2) + "s", columns.get(i)));
        }
        result.append("\n");

        // Separator
        for (int width : columnWidths) {
            result.append("-".repeat(width + 2));
        }
        result.append("\n");

        // Rows
        for (List<String> row : rows) {
            for (int i = 0; i < row.size(); i++) {
                result.append(String.format("%-" + (columnWidths[i] + 2) + "s", row.get(i)));
            }
            result.append("\n");
        }

        return result.toString();
    }
}