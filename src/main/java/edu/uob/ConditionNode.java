package edu.uob;

import java.util.List;

public abstract class ConditionNode {
    abstract boolean evaluateCondition(List<String> row, List<String> columns);
}

