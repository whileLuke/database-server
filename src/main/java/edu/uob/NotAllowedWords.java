package edu.uob;

import java.util.Arrays;
import java.util.List;

public class NotAllowedWords {
    private static final List<String> notAllowedWords = Arrays.asList(
            "USE", "CREATE", "DATABASE", "TABLE", "DROP",
            "ALTER", "INSERT", "INTO", "VALUES", "DELETE",
            "FROM", "UPDATE", "SET", "SELECT", "JOIN", "ON",
            "WHERE", "AND", "OR", "NULL", "TRUE", "FALSE"
    );

    public static boolean isNotAllowed(String word) { return notAllowedWords.contains(word.toUpperCase()); }
}
