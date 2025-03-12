package edu.uob;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class NotAllowedWords {
    private static final Set<String> RESERVED_WORDS = new HashSet<>(Arrays.asList(
            "USE", "CREATE", "DATABASE", "TABLE", "DROP", "ALTER", "INSERT",
            "INTO", "VALUES", "DELETE", "FROM", "UPDATE", "SET", "SELECT",
            "JOIN", "ON", "WHERE", "AND", "OR", "NOT", "NULL", "TRUE", "FALSE"
    ));

    public static boolean isNotAllowed(String word) {
        return RESERVED_WORDS.contains(word.toUpperCase());
    }

    public static boolean isAllowed(String name) {
        return !isNotAllowed(name);
    }
}
