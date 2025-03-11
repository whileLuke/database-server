package edu.uob;

import java.util.*;

public class ConditionParser {
    private final List<String> tokens;
    private int index = 0;

    public ConditionParser(List<String> tokens) {
        this.tokens = tokens;
    }

    public ConditionNode parse() {
        if (tokens.isEmpty()) return null;
        return parseExpression();
    }

    private ConditionNode parseExpression() {
        ConditionNode left = parseTerm();
        while (index < tokens.size() && tokens.get(index).equalsIgnoreCase("OR")) {
            index++;
            ConditionNode right = parseTerm();
            left = new LogicalCondition("OR", left, right);
        }
        return left;
    }

    private ConditionNode parseTerm() {
        ConditionNode left = parseFactor();
        while (index < tokens.size() && tokens.get(index).equalsIgnoreCase("AND")) {
            index++;
            ConditionNode right = parseFactor();
            left = new LogicalCondition("AND", left, right);
        }
        return left;
    }

    private ConditionNode parseFactor() {
        /*if (index < tokens.size() && tokens.get(index).equalsIgnoreCase("NOT")) {
            index++;
            return new NotCondition(parseFactor());
        }*/

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
