package edu.uob;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InputTokeniser {
    private static final String[] specialChars = {"(", ")", ",", ";", "!", ">", "<", "="};

    public List<String> tokenise(String input) {
        List<String> tokens = new ArrayList<>();
        String[] inputParts = input.split("'");

        for (int i = 0; i < inputParts.length; i++) {
            if (i % 2 != 0) tokens.add("'" + inputParts[i] + "'");
            else {
                String[] otherTokens = tokeniseParts(inputParts[i]);
                tokens.addAll(Arrays.asList(otherTokens));
            }
        }
        return tokens;
    }

    private String[] tokeniseParts(String input) {
        for (String specialCharacter : specialChars) input = input.replace(specialCharacter, " " + specialCharacter + " ");
        while (input.contains("  ")) input = input.replace("  ", " ");
        input = input.trim();
        if (input.isEmpty()) return new String[0];
        String[] initialTokens = input.split(" ");
        return tokeniseCompoundOperators(initialTokens).toArray(new String[0]);
    }

    private List<String> tokeniseCompoundOperators(String[] initialTokens) {
        List<String> tokensList = new ArrayList<>();
        for (int i = 0; i < initialTokens.length; i++) {
            if (i < initialTokens.length - 1 &&
                    (initialTokens[i].equals(">") || initialTokens[i].equals("<") ||
                            initialTokens[i].equals("=") || initialTokens[i].equals("!")) &&
                    initialTokens[i+1].equals("=")) {
                tokensList.add(initialTokens[i] + initialTokens[i+1]);
                i++;
            } else tokensList.add(initialTokens[i]);
        }
        return tokensList;
    }
}