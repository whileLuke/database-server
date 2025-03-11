package edu.uob;

import java.util.*;

public class ConditionEvaluator {

    public boolean isRowMatchConditions(List<String> row, List<String> conditions, List<String> columns) throws Exception {
        System.out.println("[DEBUG] Evaluating row: " + row + " against conditions: " + conditions);

        List<String> tokens = tokeniseConditions(conditions);
        System.out.println("[DEBUG] Tokenized condition: " + tokens);

        return evaluateExpression(tokens, row, columns);
    }

    private boolean evaluateCondition(String rowValue, String conditionValue, String operator) {
        System.out.println("[DEBUG] Evaluating: '" + rowValue + "' " + operator + " '" + conditionValue + "'");

        // Check for numeric comparison
        try {
            double numRowValue = Double.parseDouble(rowValue.trim());
            double numConditionValue = Double.parseDouble(conditionValue.trim());
            System.out.println("[DEBUG] Numeric Comparison: " + numRowValue + " " + operator + " " + numConditionValue);

            switch (operator) {
                case ">": return numRowValue > numConditionValue;
                case "<": return numRowValue < numConditionValue;
                case ">=": return numRowValue >= numConditionValue;
                case "<=": return numRowValue <= numConditionValue;
                case "==": return numRowValue == numConditionValue;
                case "!=": return numRowValue != numConditionValue;
            }
        } catch (NumberFormatException e) {
            System.out.println("[DEBUG] Not a number, treating as a string: " + rowValue);
        }


        // String comparison
        return switch (operator) {
            case "==" -> rowValue.trim().equals(conditionValue.trim());
            case "!=" -> !rowValue.equals(conditionValue);
            case "LIKE" -> rowValue.contains(conditionValue);
            default -> {
                System.out.println("[ERROR] Unsupported operator: " + operator);
                yield false;
            }
        };
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
            System.out.println("[DEBUG] Column '" + column + "' found at index: " + columnIndex);


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
            System.out.println("[DEBUG] Raw condition: " + condition);
            String[] parts = condition.split("(?=[()])|(?<=[()])|\\s+");
            for (String part : parts) {
                if (!part.isBlank()) {
                    tokens.add(part);
                }
            }
        }
        System.out.println("[DEBUG] Tokenized condition: " + tokens);
        return tokens;
    }


}