package edu.uob;

import java.util.List;

public class NullCondition extends ConditionNode {
    private final String columnName;

    public NullCondition(String columnName, boolean isNull) {
        this.columnName = columnName;
    }

    @Override
    public boolean evaluate(List<String> row, List<String> columns) {
        int index = columns.indexOf(columnName);
        if (index == -1) return false;
        String rowValue = row.get(index);
        return (rowValue == null || rowValue.equals("NULL") || rowValue.isEmpty());
    }
}