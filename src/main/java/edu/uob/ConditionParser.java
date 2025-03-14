package edu.uob;

import java.util.List;

public class ConditionParser {
    private final List<String> tokens;
    private int index = 0;
    private String errorMessage = null;

    public ConditionParser(List<String> tokens) { this.tokens = tokens; }

    public ConditionNode parseConditions() {
        if (tokens.isEmpty()) {
            errorMessage = "No conditions were provided.";
            return null;
        }
        return parseExpression();
    }

    public String getErrorMessage() { return errorMessage; }

    private ConditionNode parseExpression() {
        ConditionNode left = parseParenthesis();
        if (left == null) return null;

        while (index < tokens.size()) {
            String operator = tokens.get(index);
            if (!operator.equalsIgnoreCase("AND") && !operator.equalsIgnoreCase("OR")) break;
            index++;
            ConditionNode right = parseParenthesis();
            if (right == null) return null;
            left = new BoolOperatorCondition(operator.toUpperCase(), left, right);
        }
        return left;
    }

    private ConditionNode parseParenthesis() {
        if (index < tokens.size() && tokens.get(index).equals("(")) {
            index++;
            ConditionNode node = parseExpression();
            if (node == null) return null;

            if (index < tokens.size() && tokens.get(index).equals(")")) {
                index++;
                return node;
            }
            errorMessage = "[ERROR] Closing parenthesis is missing";
            return null;
        }
        return parseComparison();
    }

    private ConditionNode parseComparison() {
        if (index + 2 >= tokens.size()) {
            errorMessage = "[ERROR] An invalid comparison was attempted.";
            return null;
        }
        String columnName = tokens.get(index);
        index++;
        String operator = tokens.get(index);
        index++;
        String value = tokens.get(index);

        if (value.equals(";")) {
            errorMessage = "[ERROR] An invalid comparison was attempted.";
            return null;
        }
        index++;

        if (index < tokens.size()) {
            String nextToken = tokens.get(index);
            if (!(nextToken.equalsIgnoreCase("AND") || nextToken.equalsIgnoreCase("OR") ||
                    nextToken.equals(")") || nextToken.equals(";"))) {
                errorMessage = "[ERROR] If there are multiple conditions, there must be an AND/OR between them.";
                return null;
            }
        }

        if (value.equalsIgnoreCase("NULL")) {
            if (operator.equals("==")) return new NullCondition(columnName, true);
            else if (operator.equals("!=")) return new NullCondition(columnName, false);
        }
        return new ComparatorCondition(columnName, operator, value);
    }
}
