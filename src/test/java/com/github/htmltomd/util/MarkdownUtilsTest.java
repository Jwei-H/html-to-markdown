package com.github.htmltomd.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for MarkdownUtils.
 */
class MarkdownUtilsTest {

    @Test
    void testEscape() {
        assertEquals("\\\\", MarkdownUtils.escape("\\"));
        assertEquals("\\*\\*bold\\*\\*", MarkdownUtils.escape("**bold**"));
        assertEquals("\\[link\\]\\(url\\)", MarkdownUtils.escape("[link](url)"));
        assertEquals("\\# heading", MarkdownUtils.escape("# heading"));
        assertEquals("\\|table\\|", MarkdownUtils.escape("|table|"));
    }

    @Test
    void testNormalizeWhitespace() {
        assertEquals("single space", MarkdownUtils.normalizeWhitespace("single   space"));
        assertEquals("trim end", MarkdownUtils.normalizeWhitespace("  trim end  "));
        assertEquals("", MarkdownUtils.normalizeWhitespace(null));
        assertEquals("", MarkdownUtils.normalizeWhitespace("   "));
    }

    @Test
    void testEnsureTrailingNewlines() {
        assertEquals("text\n\n", MarkdownUtils.ensureTrailingNewlines("text", 2));
        assertEquals("text\n\n", MarkdownUtils.ensureTrailingNewlines("text\n", 2));
        assertEquals("text\n\n", MarkdownUtils.ensureTrailingNewlines("text\n\n\n", 2));
        assertEquals("\n\n", MarkdownUtils.ensureTrailingNewlines(null, 2));
    }

    @Test
    void testIndent() {
        assertEquals("  text", MarkdownUtils.indent("text", 1, 2));
        assertEquals("    text", MarkdownUtils.indent("text", 2, 2));
        assertEquals("  line1\n  line2", MarkdownUtils.indent("line1\nline2", 1, 2));
        assertEquals("text", MarkdownUtils.indent("text", 0, 2));
    }

    @Test
    void testIsBlank() {
        assertTrue(MarkdownUtils.isBlank(null));
        assertTrue(MarkdownUtils.isBlank(""));
        assertTrue(MarkdownUtils.isBlank("   "));
        assertTrue(MarkdownUtils.isBlank("\n\t"));
        assertFalse(MarkdownUtils.isBlank("text"));
        assertFalse(MarkdownUtils.isBlank(" text "));
    }
}
