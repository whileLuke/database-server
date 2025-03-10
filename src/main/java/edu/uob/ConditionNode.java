package edu.uob;

import java.util.List;

public abstract class ConditionNode {
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
        if (index == -1) {
            System.out.println("[ERROR] Column '" + column + "' not found in table!");
            return false;
        }

        String rowValue = row.get(index).trim();
        System.out.println("[DEBUG] Evaluating condition: Column = " + column + ", Row Value = '" + rowValue + "', Operator = '" + operator + "', Condition Value = '" + value + "'");

        // Check if the condition value is quoted
        String conditionValue = value;
        if ((value.startsWith("\"") && value.endsWith("\"")) ||
                (value.startsWith("'") && value.endsWith("'"))) {
            conditionValue = value.substring(1, value.length() - 1).trim();
        }

        try {
            // Attempt numeric comparison if both values are numeric
            double rowNum = Double.parseDouble(rowValue);
            double compNum = Double.parseDouble(conditionValue);
            System.out.println("[DEBUG] Numeric Comparison: " + rowNum + " " + operator + " " + compNum);

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
            // String comparison
            System.out.println("[DEBUG] String Comparison: '" + rowValue + "' " + operator + " '" + conditionValue + "'");

            return switch (operator) {
                case "==" -> rowValue.equals(conditionValue);
                case "!=" -> !rowValue.equals(conditionValue);
                case "LIKE" -> rowValue.contains(conditionValue);
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