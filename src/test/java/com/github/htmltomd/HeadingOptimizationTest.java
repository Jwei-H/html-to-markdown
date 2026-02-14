package com.github.htmltomd;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for heading optimizations including:
 * 1. Merging consecutive same-level headings.
 * 2. Removing empty headings.
 * 3. Handling image-only headings.
 */
class HeadingOptimizationTest {

    private final HtmlToMarkdownConverter converter = new HtmlToMarkdownConverter();

    @Test
    void testRemoveEmptyHeadings() {
        assertEquals("", converter.convert("<h1></h1>").trim());
        assertEquals("", converter.convert("<h2>   </h2>").trim());

        String result = converter.convert("<h1>Title</h1><h2></h2>").trim();
        assertEquals("# Title", result);
    }

    @Test
    void testImageOnlyHeadings() {
        // Image only -> remove heading attribute (no #)
        String html = "<h1><img src='img.png' alt='alt'></h1>";
        String expected = "![alt](img.png)";
        assertEquals(expected, converter.convert(html).trim());

        // Image with text -> keep heading attribute (has #)
        html = "<h1>Text <img src='img.png' alt='alt'></h1>";
        String result = converter.convert(html).trim();
        // Exact whitespace might vary, check content
        assertTrue(result.startsWith("# Text"), "Should start with heading char");
        assertTrue(result.contains("![alt](img.png)"), "Should contain image");
    }

    @Test
    void testMergeConsecutiveHeadings() {
        // Case 1: H + H
        String html = "<h1>Title A</h1><h1>Title B</h1>";
        String result = converter.convert(html).trim();
        // Expect: # Title A Title B
        assertTrue(result.contains("# Title A Title B"), "Result was: " + result);

        // Case 2: H + BR + H
        html = "<h2>Title A</h2><br><h2>Title B</h2>";
        result = converter.convert(html).trim();
        assertTrue(result.contains("## Title A Title B"), "Result was: " + result);
    }

    @Test
    void testMergeConsecutiveHeadingsWithWhitespace() {
        String html = "<h3>A</h3>   <h3>B</h3>";
        String result = converter.convert(html).trim();
        assertTrue(result.contains("### A B"), "Result was: " + result);
    }

    @Test
    void testDoNotMergeDifferentLevels() {
        String html = "<h1>A</h1><h2>B</h2>";
        String result = converter.convert(html).trim();
        assertTrue(result.contains("# A"));
        assertTrue(result.contains("## B"));
        assertFalse(result.contains("# A B"), "Different levels should not be merged");
    }

    @Test
    void testDoNotMergeWithTextInBetween() {
        String html = "<h1>A</h1>Text<h1>B</h1>";
        String result = converter.convert(html).trim();
        assertTrue(result.contains("# A"), "Should contain # A");
        assertTrue(result.contains("# B"), "Should contain # B");
        // Should NOT contain <br> merging them
        assertFalse(result.contains("A <br> B"), "Should not merge");
    }
}
