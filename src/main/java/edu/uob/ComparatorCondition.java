package edu.uob;

import java.util.List;

public class ComparatorCondition extends ConditionNode {
    private final String columnName;
    private final String comparator;
    private final String value;

    public ComparatorCondition(String columnName, String comparator, String value) {
        this.columnName = columnName;
        this.comparator = comparator;
        this.value = value;
    }

    @Override
    boolean evaluateCondition(List<String> row, List<String> columns) {
        int index = columns.indexOf(columnName);
        if (index == -1) return false;
        String rowValue = row.get(index).trim();
        String conditionValue = value;
        if (isInQuotes(value)) conditionValue = value.substring(1, value.length() - 1).trim();
        try {
            double rowNum = Double.parseDouble(rowValue);
            double compNum = Double.parseDouble(conditionValue);
            return switch (comparator) {
                case ">" -> rowNum > compNum;
                case ">=" -> rowNum >= compNum;
                case "<" -> rowNum < compNum;
                case "<=" -> rowNum <= compNum;
                case "==" -> rowNum == compNum;
                case "!=" -> rowNum != compNum;
                default -> false;
            };
        } catch (NumberFormatException ignored) {
            return switch (comparator) {
                case "==" -> rowValue.equals(conditionValue);
                case "!=" -> !rowValue.equals(conditionValue);
                case "LIKE" -> rowValue.contains(conditionValue);
                default -> false;
            };
        }
    }

    boolean isInQuotes(String value){
        return (value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"));
    }
}
