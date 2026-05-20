package com.airesume.screening.utils;

import java.util.Locale;
import java.util.StringJoiner;

public final class TextFormatUtils {

    private TextFormatUtils() {}

    /**
     * Fixes common UTF-8 mojibake and normalizes dash-like characters for display.
     */
    public static String normalizeDisplayText(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }
        String text = input;
        text = text.replace("â€“", "–");
        text = text.replace("â€”", "—");
        text = text.replace("â€˜", "'");
        text = text.replace("â€™", "'");
        text = text.replace("â€œ", "\"");
        text = text.replace("â€\u009d", "\"");
        text = text.replace("â€¢", "•");
        text = text.replace('\u00A0', ' ');
        text = text.replace("\u00E2\u0080\u0093", "–");
        text = text.replace("\u00E2\u0080\u0094", "—");
        return text.trim();
    }

    /**
     * "PRIYA SHARMA" → "Priya Sharma"; preserves spacing between tokens.
     */
    public static String toTitleCaseName(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }
        String[] tokens = input.trim().split("\\s+");
        StringJoiner joiner = new StringJoiner(" ");
        for (String token : tokens) {
            if (token.isEmpty()) {
                continue;
            }
            String lower = token.toLowerCase(Locale.ROOT);
            joiner.add(Character.toUpperCase(lower.charAt(0)) + lower.substring(1));
        }
        return joiner.toString();
    }
}
