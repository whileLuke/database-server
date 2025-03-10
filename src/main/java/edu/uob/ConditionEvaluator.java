package edu.uob;


import java.util.*;


public class ConditionEvaluator {
    public boolean isRowMatchConditions(List<String> row, List<String> conditions, List<String> columns)
            throws Exception {
        boolean rowMatches = false;
        for (String condition : conditions) {
            ConditionParts conditionParts = parseCondition(condition);
            if (conditionParts == null) {
                System.out.println(condition + " is not a valid condition");
                return false;
            }
            String columnName = conditionParts.columnName;
            String operator = conditionParts.operator;
            String value = conditionParts.value;
            int columnIndex = columns.indexOf(columnName);
            if (columnIndex == -1){
                System.out.println(condition + " is not a valid column");
                return false;
            }
            String rowValue = row.get(columnIndex);
            boolean conditionResult = evaluateCondition(rowValue, value, operator);
            System.out.println("Checking row: " + rowValue + " column: " + columnName + " operator: " + operator + " value: " + value + "RESULT:" + conditionResult);
            if (conditionResult) rowMatches = true;
        }
        return rowMatches;
    }

    private ConditionNode parseConditionTree(List<String> conditions) {
        if (conditions.size() == 1) {
            return new ConditionNode(conditions.get(0));
        }
        Stack<ConditionNode> stack = new Stack<>();
        for (String token : conditions) {
            if (token.equals(")")) {
                List<ConditionNode> groupedNodes = new ArrayList<>();
                while (!stack.isEmpty() && !stack.peek().value.equals("(")) {
                    groupedNodes.add(stack.pop());
                }
                if (!stack.isEmpty() && stack.peek().value.equals("(")) stack.pop(); // Remove '('
                ConditionNode groupedNode = new ConditionNode("AND", groupedNodes);
                stack.push(groupedNode);
            } else {
                stack.push(new ConditionNode(token));
            }
        }

        while (stack.size() > 1) {
            ConditionNode right = stack.pop();
            ConditionNode operator = stack.pop();
            ConditionNode left = stack.pop();
            stack.push(new ConditionNode(operator.value, left, right));
        }
        return stack.isEmpty() ? null : stack.pop();
    }

    private boolean evaluateConditionTree(ConditionNode node, List<String> row, List<String> columns) {
        if (node.isLeaf()) {
            boolean result = compareCondition(node.value, row, columns);
            System.out.println("[DEBUG] Leaf condition: " + node.value + " -> " + result);
            return result;
        }
        boolean result;
        if (node.value.equals("AND")) {
            result = node.children.stream().allMatch(child -> evaluateConditionTree(child, row, columns));
            System.out.println("[DEBUG] AND Result: " + result);
            return result;
        }
        if (node.value.equals("OR")) {
            result = node.children.stream().anyMatch(child -> evaluateConditionTree(child, row, columns));
            System.out.println("[DEBUG] OR Result: " + result);
            return result;
        }
        return false;

    }

    private boolean compareCondition(String condition, List<String> row, List<String> columns) {
        ConditionParts parts = parseCondition(condition);
        if (parts == null) return false;
        String columnName = parts.columnName();
        String operator = parts.operator();
        String value = parts.value();
        int columnIndex = columns.indexOf(columnName);
        if (columnIndex == -1) return false;
        String rowValue = row.get(columnIndex);
        return evaluateCondition(rowValue, value, operator);
    }


    private ConditionParts parseCondition(String condition) {
        String[] operators = {"==", "!=", ">=", "<=", ">", "<", "LIKE"};
        for (String operator : operators) {
            int index = condition.indexOf(operator);
            if (index != -1) {
                String column = condition.substring(0, index).trim();
                String value = condition.substring(index + operator.length()).trim();
                return new ConditionParts(column, operator, value);
            }
        }
        return null;
    }


    private boolean evaluateCondition(String rowValue, String conditionValue, String operator) {
        if ((conditionValue.startsWith("\"") && conditionValue.endsWith("\"")) ||
                (conditionValue.startsWith("'") && conditionValue.endsWith("'"))) {
            conditionValue = conditionValue.substring(1, conditionValue.length() - 1);
        }
        System.out.println("[DEBUG] Evaluating condition " + conditionValue + " to " + operator + " of " + rowValue);
        switch (operator) {
            case "==":
                return rowValue.equals(conditionValue);
            case "!=":
                return !rowValue.equals(conditionValue);
            case ">":
                return compareNumbers(rowValue, conditionValue, operator);
            case "<":
                return compareNumbers(rowValue, conditionValue, operator);
            case ">=":
                return compareNumbers(rowValue, conditionValue, operator);
            case "<=":
                return compareNumbers(rowValue, conditionValue, operator);
            case "LIKE":
                return rowValue.contains(conditionValue);
            default:
                return false;
        }
    }

    private boolean compareNumbers(String rowValue, String conditionValue, String operator) {
        try {
            double num1 = Double.parseDouble(rowValue);
            double num2 = Double.parseDouble(conditionValue);
            System.out.println("[DEBUG] Number comparison:" +  num1 + " " + operator + " " + num2);
            return switch (operator) {
                case ">" -> num1 > num2;
                case "<" -> num1 < num2;
                case ">=" -> num1 >= num2;
                case "<=" -> num1 <= num2;
                default -> false;
            };
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Failed to parse number: " + rowValue + " or " + conditionValue);
            return false;
        }
    }


    // ==============================

    // **STEP 4: HELPER CLASSES**

    // ==============================

    private record ConditionParts(String columnName, String operator, String value) {}


    private static class ConditionNode {

        String value;

        List<ConditionNode> children;


        ConditionNode(String value) {

            this.value = value;

            this.children = new ArrayList<>();

        }


        ConditionNode(String value, ConditionNode left, ConditionNode right) {

            this.value = value;

            this.children = Arrays.asList(left, right);

        }


        ConditionNode(String value, List<ConditionNode> children) {

            this.value = value;

            this.children = children;

        }


        boolean isLeaf() {

            return children.isEmpty();

        }

    }

}