package edu.uob;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Tokeniser {
    private static final String[] SPECIAL_CHARACTERS = {"(", ")", ",", ";", "!", ">", "<", "="};

    public List<String> tokenise(String input) {
        List<String> tokens = new ArrayList<>();
        String[] fragments = input.split("'");

        for (int i = 0; i < fragments.length; i++) {
            if (i % 2 != 0) {
                // This is inside quotes - keep as a single token
                tokens.add("'" + fragments[i] + "'");
            } else {
                // This is outside quotes - tokenize normally
                String[] nextBatchOfTokens = tokeniseFragment(fragments[i]);
                tokens.addAll(Arrays.asList(nextBatchOfTokens));
            }
        }
        return tokens;
    }

    private String[] tokeniseFragment(String input) {
        for (String specialCharacter : SPECIAL_CHARACTERS) {
            input = input.replace(specialCharacter, " " + specialCharacter + " ");
        }

        // Replace multiple spaces with a single space
        while (input.contains("  ")) {
            input = input.replace("  ", " ");
        }

        input = input.trim();
        if (input.isEmpty()) {
            return new String[0];
        }

        // Split by spaces
        String[] initialTokens = input.split(" ");

        // Process compound operators like >=, <=, !=, ==
        return tokeniseCompoundOperators(initialTokens).toArray(new String[0]);
    }

    private List<String> tokeniseCompoundOperators(String[] initialTokens) {
        List<String> tokensList = new ArrayList<>();

        for (int i = 0; i < initialTokens.length; i++) {
            // Check for compound operators
            if (i < initialTokens.length - 1 &&
                    (initialTokens[i].equals(">") || initialTokens[i].equals("<") ||
                            initialTokens[i].equals("=") || initialTokens[i].equals("!")) &&
                    initialTokens[i+1].equals("=")) {

                tokensList.add(initialTokens[i] + initialTokens[i+1]);
                i++; // Skip the next token as we've combined it
            } else {
                tokensList.add(initialTokens[i]);
            }
        }

        return tokensList;
    }
}