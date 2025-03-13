package edu.uob;

import java.util.List;

public class BoolOperatorCondition extends ConditionNode {
    private final String operator;
    private final ConditionNode left;
    private final ConditionNode right;

    public BoolOperatorCondition(String operator, ConditionNode left, ConditionNode right) {
        this.operator = operator;
        this.left = left;
        this.right = right;
    }

    @Override
    boolean evaluateCondition(List<String> row, List<String> columns) {
        return switch (operator.toUpperCase()) {
            case "AND" -> left.evaluateCondition(row, columns) && right.evaluateCondition(row, columns);
            case "OR" -> left.evaluateCondition(row, columns) || right.evaluateCondition(row, columns);
            default -> false;
        };
    }
}
