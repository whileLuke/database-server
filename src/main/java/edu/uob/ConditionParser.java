package edu.uob;

import java.util.*;

public class ConditionParser {
    private final List<String> tokens;
    private int index = 0;

    public ConditionParser(List<String> tokens) {
        this.tokens = tokens;
    }

    public ConditionNode parse() {
        return parseOr();
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
        if (tokens.get(index).equals("(")) {
            index++;  // Skip '('
            ConditionNode node = parseOr();
            index++;  // Skip ')'
            return node;
        }
        return parseCondition();
    }

    private ConditionNode parseCondition() {
        String column = tokens.get(index++);
        String operator = tokens.get(index++);
        String value = tokens.get(index++);
        return new SimpleCondition(column, operator, value);
    }

    private List<String> tokenize(String conditionString) {
        return Arrays.asList(conditionString.replace("(", " ( ").replace(")", " ) ").split("\\s+"));
    }
}