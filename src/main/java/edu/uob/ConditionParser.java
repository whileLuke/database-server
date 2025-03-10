package edu.uob;

import java.util.*;

public class ConditionParser {
    private final List<String> tokens;
    private int index = 0;

    public ConditionParser(List<String> tokens) {
        this.tokens = tokens;
    }

    public ConditionNode parse() {
        if (tokens.isEmpty()) {
            System.out.println("[ERROR] No tokens to parse in WHERE clause.");
            return null;
        }
        System.out.println("[DEBUG] Parsing WHERE conditions: " + tokens);

        ConditionNode node = parseOr();

        if (node == null) {
            System.out.println("[ERROR] Condition tree was NOT built correctly!");
        } else {
            System.out.println("[DEBUG] Condition tree successfully built.");
        }

        return node;
    }

    private ConditionNode parseOr() {
        ConditionNode left = parseAnd();
        while (index < tokens.size() && tokens.get(index).equalsIgnoreCase("OR")) {
            index++;
            ConditionNode right = parseAnd();
            left = new LogicalCondition("OR", left, right);
        }
        return left;
    }

    private ConditionNode parseAnd() {
        ConditionNode left = parseNot();
        while (index < tokens.size() && tokens.get(index).equalsIgnoreCase("AND")) {
            index++;
            ConditionNode right = parseNot();
            left = new LogicalCondition("AND", left, right);
        }
        return left;
    }

    private ConditionNode parseNot() {
        if (index < tokens.size() && tokens.get(index).equalsIgnoreCase("NOT")) {
            index++;
            return new NotCondition(parsePrimary());
        }
        return parsePrimary();
    }

    private ConditionNode parsePrimary() {
        if (index < tokens.size() && tokens.get(index).equals("(")) {
            index++;  // Skip '('
            ConditionNode node = parseOr();
            index++;  // Skip ')'
            return node;
        }
        return parseCondition();
    }

    private ConditionNode parseCondition() {
        if (index + 2 >= tokens.size()) {
            System.out.println("[ERROR] Incomplete condition at index " + index);
            return null;
        }

        String column = tokens.get(index++);
        String operator = tokens.get(index++);

        // Handle quoted string values (preserve the quotes)
        String value = tokens.get(index++);

        System.out.println("[DEBUG] Created condition: " + column + " " + operator + " " + value);
        return new SimpleCondition(column, operator, value);
    }
}