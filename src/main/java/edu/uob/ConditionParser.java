package edu.uob;

import java.util.List;

public class ConditionParser {
    private final List<String> tokens;
    private int index = 0;

    public ConditionParser(List<String> tokens) { this.tokens = tokens; }

    public ConditionNode parse() {
        if (tokens.isEmpty()) return null;
        return parseExpression();
    }

    private ConditionNode parseExpression() {
        ConditionNode left = parseParenthesis();
        while (index < tokens.size()) {
            String operator = tokens.get(index);
            if (!operator.equalsIgnoreCase("AND") && !operator.equalsIgnoreCase("OR")) break;
            index++;
            ConditionNode right = parseParenthesis();
            left = new BoolOperatorCondition(operator.toUpperCase(), left, right);
        }
        return left;
    }

    private ConditionNode parseParenthesis() {
        if (index < tokens.size() && tokens.get(index).equals("(")) {
            index++;
            ConditionNode node = parseExpression();
            if (index < tokens.size() && tokens.get(index).equals(")")) {
                index++;
                return node;
            }
            throw new RuntimeException("Missing closing parenthesis");
        }
        return parseComparison();
    }

    private ConditionNode parseComparison() {
        if (index + 2 >= tokens.size()) return null;
        String columnName = tokens.get(index);
        index++;
        String operator = tokens.get(index);
        index++;
        String value = tokens.get(index);
        index++;
        if (value.equalsIgnoreCase("NULL")) {
            if (operator.equals("==")) return new NullCondition(columnName, true);
            else if (operator.equals("!=")) return new NullCondition(columnName, false);
        }
        return new ComparatorCondition(columnName, operator, value);
    }
}
