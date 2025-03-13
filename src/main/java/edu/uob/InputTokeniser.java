package edu.uob;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InputTokeniser {
    private static final String[] special_chars = {"(", ")", ",", ";", "!", ">", "<", "="};

    public List<String> tokenise(String input) {
        List<String> tokens = new ArrayList<>();
        //boolean inQuotes = false;
        //boolean inDoubleQuotes = false;
        String[] inQuotesParts = input.split("'");
        List<String> betweenQuotes = new ArrayList<>();

        for (int i = 0; i < inQuotesParts.length; i++) {
            if (i % 2 != 0) betweenQuotes.add("'" + inQuotesParts[i] + "'");
            else {
                String[] inDoubleQuotesParts = inQuotesParts[i].split("\"");
                for (int j = 0; j < inDoubleQuotesParts.length; j++) {
                    if (j % 2 != 0) betweenQuotes.add("\"" + inDoubleQuotesParts[j] + "\"");
                    else {
                        String[] otherTokens = tokeniseParts(inDoubleQuotesParts[j]);
                        betweenQuotes.addAll(Arrays.asList(otherTokens));
                    }
                }
            }
        }
        return betweenQuotes;
    }

    private String[] tokeniseParts(String input) {
        for (String specialCharacter : special_chars) input = input.replace(specialCharacter, " " + specialCharacter + " ");
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