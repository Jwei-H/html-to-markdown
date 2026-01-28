package com.github.htmltomd.util;

/**
 * Utility class for Markdown text processing and formatting.
 */
public class MarkdownUtils {

    private MarkdownUtils() {
        // Utility class
    }

    /**
     * Escapes special Markdown characters in text.
     * 
     * @param text the text to escape
     * @return escaped text
     */
    public static String escape(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        // Escape special Markdown characters
        return text.replace("\\", "\\\\")
                .replace("`", "\\`")
                .replace("*", "\\*")
                .replace("_", "\\_")
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("#", "\\#")
                .replace("+", "\\+")
                .replace("-", "\\-")
                .replace(".", "\\.")
                .replace("!", "\\!")
                .replace("|", "\\|");
    }

    /**
     * Normalizes whitespace in text.
     * Converts multiple spaces to single space and trims.
     * 
     * @param text the text to normalize
     * @return normalized text
     */
    public static String normalizeWhitespace(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("\\s+", " ").trim();
    }

    /**
     * Ensures text ends with specified number of newlines.
     * 
     * @param text  the text
     * @param count the number of newlines
     * @return text with trailing newlines
     */
    public static String ensureTrailingNewlines(String text, int count) {
        if (text == null) {
            return "\n".repeat(count);
        }

        String trimmed = text.replaceAll("\\n+$", "");
        return trimmed + "\n".repeat(count);
    }

    /**
     * Indents text by the specified level.
     * 
     * @param text           the text to indent
     * @param level          the indentation level
     * @param spacesPerLevel spaces per indentation level
     * @return indented text
     */
    public static String indent(String text, int level, int spacesPerLevel) {
        if (text == null || text.isEmpty() || level <= 0) {
            return text;
        }

        String indentation = " ".repeat(level * spacesPerLevel);
        return indentation + text.replace("\n", "\n" + indentation);
    }

    /**
     * Checks if a string is blank (null, empty, or whitespace only).
     * 
     * @param text the text to check
     * @return true if blank
     */
    public static boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }
}
