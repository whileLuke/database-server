package edu.uob;

import java.util.List;

public class ConditionEvaluator {

    public boolean isRowMatchConditions(List<String> row, List<String> conditions, List<String> columns)
            throws Exception {
        for (String condition : conditions) {
            ConditionParts parts = parseCondition(condition);
            if (parts == null) {
                throw new Exception("Invalid condition format: " + condition);
            }

            String columnName = parts.getColumnName();
            String operator = parts.getOperator();
            String value = parts.getValue();

            int columnIndex = columns.indexOf(columnName);
            if (columnIndex == -1) {
                throw new Exception("Column not found: " + columnName);
            }

            String rowValue = row.get(columnIndex);

            if (!evaluateCondition(rowValue, value, operator)) {
                return false;
            }
        }
        return true;
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
        // Remove surrounding quotes from conditionValue if needed
        if ((conditionValue.startsWith("\"") && conditionValue.endsWith("\"")) ||
                (conditionValue.startsWith("'") && conditionValue.endsWith("'"))) {
            conditionValue = conditionValue.substring(1, conditionValue.length() - 1);
        }

        switch (operator) {
            case "==":
                return rowValue.equals(conditionValue);
            case "!=":
                return !rowValue.equals(conditionValue);
            case ">":
                try {
                    return Double.parseDouble(rowValue) > Double.parseDouble(conditionValue);
                } catch (NumberFormatException e) {
                    return false;
                }
            case "<":
                try {
                    return Double.parseDouble(rowValue) < Double.parseDouble(conditionValue);
                } catch (NumberFormatException e) {
                    return false;
                }
            case ">=":
                try {
                    return Double.parseDouble(rowValue) >= Double.parseDouble(conditionValue);
                } catch (NumberFormatException e) {
                    return false;
                }
            case "<=":
                try {
                    return Double.parseDouble(rowValue) <= Double.parseDouble(conditionValue);
                } catch (NumberFormatException e) {
                    return false;
                }
            case "LIKE":
                String regex = conditionValue.replace("%", ".*").replace("_", ".");
                return rowValue.matches(regex);
            default:
                return false;
        }
    }

    // Helper class to store parsed condition parts
    private static class ConditionParts {
        private final String columnName;
        private final String operator;
        private final String value;

        public ConditionParts(String columnName, String operator, String value) {
            this.columnName = columnName;
            this.operator = operator;
            this.value = value;
        }

        public String getColumnName() {
            return columnName;
        }

        public String getOperator() {
            return operator;
        }

        public String getValue() {
            return value;
        }
    }
}