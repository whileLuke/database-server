package edu.uob;

import java.util.List;

public class NullCondition extends ConditionNode {
    private final String columnName;
    private final boolean isNull; // or equalsNull or something

    public NullCondition(String columnName, boolean isNull) {
        this.columnName = columnName;
        this.isNull = isNull;
    }

    @Override
    public boolean evaluate(List<String> row, List<String> columns) {
        int index = columns.indexOf(columnName);
        if (index == -1) return false;
        String value = row.get(index);
        boolean valueIsNull = (value == null || value.equals("NULL") || value.isEmpty());
        return (isNull == valueIsNull);
    }
}