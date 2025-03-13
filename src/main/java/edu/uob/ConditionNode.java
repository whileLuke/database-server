package edu.uob;

import java.util.List;

public abstract class ConditionNode {
    abstract boolean evaluate(List<String> row, List<String> columns);
}

class SimpleCondition extends ConditionNode {
    private final String columnName;
    private final String comparator;
    private final String value;

    public SimpleCondition(String columnName, String comparator, String value) {
        this.columnName = columnName;
        this.comparator = comparator;
        this.value = value;
    }

    @Override
    boolean evaluate(List<String> row, List<String> columns) {
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

class LogicalCondition extends ConditionNode {
    private final String operator;
    private final ConditionNode left;
    private final ConditionNode right;

    public LogicalCondition(String operator, ConditionNode left, ConditionNode right) {
        this.operator = operator;
        this.left = left;
        this.right = right;
    }

    @Override
    boolean evaluate(List<String> row, List<String> columns) {
        return switch (operator.toUpperCase()) {
            case "AND" -> left.evaluate(row, columns) && right.evaluate(row, columns);
            case "OR" -> left.evaluate(row, columns) || right.evaluate(row, columns);
            default -> false;
        };
    }
}
