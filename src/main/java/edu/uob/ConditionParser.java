package edu.uob;

import java.util.*;

public class ConditionParser {
    private final List<String> tokens;
    private int index = 0;

    public ConditionParser(List<String> tokens) { this.tokens = tokens; }

    public ConditionNode parse() {
        if (tokens.isEmpty()) return null;
        return parseSequence();
    }

    private ConditionNode parseSequence() {
        ConditionNode left = parseParenthesis();

        while (index < tokens.size() &&
                (tokens.get(index).equalsIgnoreCase("AND") ||
                        tokens.get(index).equalsIgnoreCase("OR"))) {
            String operator = tokens.get(index);
            index++;
            ConditionNode right = parseParenthesis();
            left = new LogicalCondition(operator, left, right);
        }

        return left;
    }

    private ConditionNode parseParenthesis() {
        if (index < tokens.size() && tokens.get(index).equals("(")) {
            index++;
            ConditionNode node = parseSequence();
            if (index < tokens.size() && tokens.get(index).equals(")")) {
                index++;
                return node;
            }
            throw new RuntimeException("Missing closing parenthesis");
        }
        return parseComparison();
    }

    private ConditionNode parseComparison() {
        if (index + 2 >= tokens.size()) {
            throw new RuntimeException("Incomplete comparison at index " + index);
        }

        String column = tokens.get(index++);
        String operator = tokens.get(index++);
        String value = tokens.get(index++);

        if (value.equalsIgnoreCase("NULL")) {
            if (operator.equals("=")) {
                return new NullCondition(column, true);
            } else if (operator.equals("!=") || operator.equals("<>")) {
                return new NullCondition(column, false);
            }
        }
        return new SimpleCondition(column, operator, value);
    }
}
