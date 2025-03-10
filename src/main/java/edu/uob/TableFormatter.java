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
}