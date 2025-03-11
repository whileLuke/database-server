package edu.uob;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class ConditionHandler {

    // Main entry point to evaluate a row against conditions
    public static boolean evaluateRowAgainstConditions(List<String> row, List<String> conditions, List<String> columns) {
        try {
            // Parse conditions into condition tree
            ConditionNode rootNode = parseConditions(conditions);
            if (rootNode == null) return true; // No conditions means all rows match

            // Evaluate condition tree against row
            return rootNode.evaluate(row, columns);
        } catch (Exception e) {
            return false;
        }
    }

    // Parse conditions into a tree structure
    private static ConditionNode parseConditions(List<String> conditions) {
        if (conditions == null || conditions.isEmpty()) return null;

        List<String> tokens = tokenizeConditions(conditions);
        ConditionParser parser = new ConditionParser(tokens);
        return parser.parse();
    }

    // Convert condition strings to a flat list of tokens
    private static List<String> tokenizeConditions(List<String> conditions) {
        List<String> tokens = new ArrayList<>();
        for (String condition : conditions) {

            condition = condition.trim();
            // Handle logical operators
            condition = condition.replaceAll("(?i)\\bAND\\b", " AND ");
            condition = condition.replaceAll("(?i)\\bOR\\b", " OR ");
            condition = condition.replaceAll("(?i)\\bNOT\\b", " NOT ");

            // Handle parentheses
            condition = condition.replace("(", " ( ").replace(")", " ) ");

            // Split and add non-empty tokens
            for (String token : condition.split("\\s+")) {
                if (!token.isEmpty()) {
                    tokens.add(token);
                }
            }
        }

        return tokens;
    }

    // Helper method to determine if a value is numeric
    public static boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) return false;
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Helper method to compare values based on operator
    public static boolean compareValues(String value1, String value2, String operator) {

        // Process string literals by removing quotes
        if ((value2.startsWith("'") && value2.endsWith("'")) ||
                (value2.startsWith("\"") && value2.endsWith("\""))) {
            value2 = value2.substring(1, value2.length() - 1);
        }

        // Numeric comparison if both values are numeric
        if (isNumeric(value1) && isNumeric(value2)) {
            double num1 = Double.parseDouble(value1);
            double num2 = Double.parseDouble(value2);

            return switch (operator.toUpperCase()) {
                case ">" -> num1 > num2;
                case ">=" -> num1 >= num2;
                case "<" -> num1 < num2;
                case "<=" -> num1 <= num2;
                case "==" -> num1 == num2;
                case "!=" -> num1 != num2;
                default -> false;
            };
        }

        // String comparison
        return switch (operator.toUpperCase()) {
            case "==" -> value1.equals(value2);
            case "!=" -> !value1.equals(value2);
            case "LIKE" -> value1.contains(value2);
            default -> false;
        };
    }
}