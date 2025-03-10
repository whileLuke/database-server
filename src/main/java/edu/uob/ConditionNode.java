package edu.uob;

import java.util.List;

abstract class ConditionNode {
    abstract boolean evaluate(List<String> row, List<String> columns);
}

class SimpleCondition extends ConditionNode {
    private final String column;
    private final String operator;
    private final String value;

    public SimpleCondition(String column, String operator, String value) {
        this.column = column;
        this.operator = operator;
        this.value = value;
    }

    @Override
    boolean evaluate(List<String> row, List<String> columns) {
        int index = columns.indexOf(column);
        if (index == -1) return false;
        String rowValue = row.get(index).trim();
        try {
            double rowNum = Double.parseDouble(rowValue);
            double compNum = Double.parseDouble(value);
            return switch (operator) {
                case ">" -> rowNum > compNum;
                case ">=" -> rowNum >= compNum;
                case "<" -> rowNum < compNum;
                case "<=" -> rowNum <= compNum;
                case "==" -> rowNum == compNum;
                case "!=" -> rowNum != compNum;
                default -> false;
            };
        } catch (NumberFormatException ignored) {
            return switch (operator) {
                case "==" -> rowValue.equals(value.replace("\"", ""));
                case "!=" -> !rowValue.equals(value.replace("\"", ""));
                case "LIKE" -> rowValue.contains(value.replace("\"", ""));
                default -> false;
            };
        }
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
        return switch (operator) {
            case "AND" -> left.evaluate(row, columns) && right.evaluate(row, columns);
            case "OR" -> left.evaluate(row, columns) || right.evaluate(row, columns);
            default -> false;
        };
    }
}

class NotCondition extends ConditionNode {
    private final ConditionNode condition;
    public NotCondition(ConditionNode condition) {
        this.condition = condition;
    }

    @Override
    boolean evaluate(List<String> row, List<String> columns) {
        return !condition.evaluate(row, columns);
    }
}