package com.example.stepappv3.util;

import android.util.Log;

public class HelperFunctions {

    // Helper method to safely remove quotes from a token.
    public static String unquoter(String text) {
        if (text != null && text.length() > 1 && text.startsWith("\"") && text.endsWith("\"")) {
            return text.substring(1, text.length() - 1);
        }
        return text;
    }

// --- NEW HELPER METHODS for Safe Parsing ---

    /**
     * Safely parses a string to a double, returning 0.0 if the string is null, empty, or invalid.
     */
    public static double safeParseDouble(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            Log.e("SafeParse", "Could not parse double: '" + text + "'", e);
            return 0.0;
        }
    }

    /**
     * Safely parses a string to an integer, returning 0 if the string is null, empty, or invalid.
     */
    public static int safeParseInt(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            Log.e("SafeParse", "Could not parse int: '" + text + "'", e);
            return 0;
        }
    }
}