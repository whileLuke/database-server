package edu.uob;

import java.util.*;

public class ConditionEvaluator {

    public boolean isRowMatchConditions(List<String> row, List<String> conditions, List<String> columns) throws Exception {
        List<String> tokens = tokeniseConditions(conditions);
        return evaluateExpression(tokens, row, columns);
    }

    private boolean evaluateCondition(String rowValue, String conditionValue, String operator) {
        System.out.println("[DEBUG] Evaluating: '" + rowValue + "' " + operator + " '" + conditionValue + "'");

        // Try parsing as a number
        try {
            double numRowValue = Double.parseDouble(rowValue);
            double numConditionValue = Double.parseDouble(conditionValue);

            switch (operator) {
                case ">": return numRowValue > numConditionValue;
                case "<": return numRowValue < numConditionValue;
                case ">=": return numRowValue >= numConditionValue;
                case "<=": return numRowValue <= numConditionValue;
            }
        } catch (NumberFormatException e) {
            // Not a number, do string comparison instead
            System.out.println("[DEBUG] Treating as string: '" + rowValue + "' " + operator + " '" + conditionValue + "'");
        }

        switch (operator) {
            case "==":
                System.out.println("[DEBUG] Comparing Strings: '" + rowValue.trim() + "' == '" + conditionValue.trim() + "'");
                return rowValue.trim().equals(conditionValue.trim());
            case "!=": return !rowValue.equals(conditionValue);
            case "LIKE": return rowValue.contains(conditionValue);
            default:
                System.out.println("[ERROR] Unsupported operator: " + operator);
                return false;
        }
    }

    private boolean evaluateExpression(List<String> tokens, List<String> row, List<String> columns) {
        boolean result = false;
        boolean pendingAnd = false;

        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);

            if (token.equals("and") || token.equals("or")) {
                pendingAnd = token.equals("and");
                continue;
            }

            // Extract condition
            String column = tokens.get(i);
            String operator = tokens.get(i + 1);
            String value = tokens.get(i + 2);
            i += 2;

            int columnIndex = columns.indexOf(column);
            if (columnIndex == -1) {
                System.out.println("[ERROR] Column not found: " + column);
                return false;
            }

            boolean conditionResult = evaluateCondition(row.get(columnIndex), value, operator);
            System.out.println("[DEBUG] Evaluating condition: " + column + " " + operator + " " + value + " -> " + conditionResult);

            // Apply AND/OR logic
            if (pendingAnd) {
                result = result && conditionResult;
            } else {
                result = result || conditionResult;
            }
        }

        System.out.println("[DEBUG] Final evaluation result: " + result);
        return result;
    }








    private void applyOperator(Stack<Boolean> values, String operator) {
        boolean b = values.pop();
        boolean a = values.pop();
        if (operator.equals("and")) {
            values.push(a && b);
        } else if (operator.equals("or")) {
            values.push(a || b);
        }
    }

    private int precedence(String op) {
        if (op.equals("and")) return 2;
        if (op.equals("or")) return 1;
        return 0;
    }

    private List<String> tokeniseConditions(List<String> conditions) {
        List<String> tokens = new ArrayList<>();
        for (String condition : conditions) {
            String[] parts = condition.split("(?=[()])|(?<=[()])|\\s+");
            for (String part : parts) {
                if (!part.isBlank()) {
                    tokens.add(part);
                }
            }
        }
        return tokens;
    }

}