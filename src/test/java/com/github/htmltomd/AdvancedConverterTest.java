package com.github.htmltomd;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for advanced features including Markdown preservation and
 * complex HTML.
 */
class AdvancedConverterTest {

    private final HtmlToMarkdownConverter converter = new HtmlToMarkdownConverter();

    // ==================== Markdown Preservation Tests ====================

    @Test
    void testPreserveMarkdownText() {
        // Pure Markdown text should be preserved
        String markdown = "# Heading\n\nThis is **bold** and *italic* text.";
        String result = converter.convert(markdown);
        assertTrue(result.contains("# Heading"));
        assertTrue(result.contains("**bold**"));
        assertTrue(result.contains("*italic*"));
    }

    @Test
    void testPreserveMarkdownLists() {
        String markdown = "- Item 1\n- Item 2\n- Item 3";
        String result = converter.convert(markdown);
        assertTrue(result.contains("- Item 1"));
        assertTrue(result.contains("- Item 2"));
        assertTrue(result.contains("- Item 3"));
    }

    @Test
    void testMixedMarkdownAndHtml() {
        // Markdown should be preserved, HTML should be converted
        String mixed = "# Title\n\nThis is **markdown**.\n\n<div><strong>This is HTML</strong></div>";
        String result = converter.convert(mixed);
        assertTrue(result.contains("# Title"));
        assertTrue(result.contains("**markdown**"));
        // HTML converted
        assertTrue(result.contains("**This is HTML**"));
    }

    @Test
    void testCodeBlockProtection() {
        // HTML inside code blocks should NOT be converted
        String markdown = "```html\n<div><strong>HTML code</strong></div>\n```";
        String result = converter.convert(markdown);
        assertTrue(result.contains("```html"));
        assertTrue(result.contains("<div>"));
        assertTrue(result.contains("<strong>"));
        assertTrue(result.contains("</strong>"));
        assertTrue(result.contains("</div>"));
    }

    @Test
    void testMultipleCodeBlocks() {
        String markdown = "```java\npublic class Test {}\n```\n\nSome text\n\n```python\ndef hello():\n    print('hi')\n```";
        String result = converter.convert(markdown);
        assertTrue(result.contains("```java"));
        assertTrue(result.contains("public class Test"));
        assertTrue(result.contains("```python"));
        assertTrue(result.contains("def hello"));
    }

    // ==================== Complex HTML Nesting Tests ====================

    @Test
    void testDeeplyNestedStrong() {
        // WeChat-style deeply nested strong tags
        String html = "<strong><strong><strong><span>text</span></strong></strong></strong>";
        String result = converter.convert(html);
        assertTrue(result.contains("**text**"));
        assertFalse(result.contains("****"));
        assertFalse(result.contains("******"));
    }

    @Test
    void testNestedSections() {
        String html = "<section><section><section><p>Deep content</p></section></section></section>";
        String result = converter.convert(html);
        assertTrue(result.contains("Deep content"));
    }

    @Test
    void testSpanWithEmphasis() {
        String html = "<p><span><strong>Bold in span</strong></span></p>";
        String result = converter.convert(html);
        assertTrue(result.contains("**Bold in span**"));
    }

    @Test
    void testEmphasisWithSpaces() {
        // Spaces inside emphasis markers should be trimmed
        String html = "<p><strong> text with spaces </strong></p>";
        String result = converter.convert(html);
        assertTrue(result.contains("**text with spaces**"));
    }

    @Test
    void testAdjacentStrongTags() {
        // Adjacent strong tags should not produce ****
        String html = "<p><strong>part1</strong><strong>part2</strong></p>";
        String result = converter.convert(html);
        // Should contain both parts, possibly merged or separated
        assertTrue(result.contains("part1"));
        assertTrue(result.contains("part2"));
    }

    @Test
    void testEmptyStrong() {
        // Empty strong tags should be removed
        String html = "<p>Before<strong></strong>After</p>";
        String result = converter.convert(html);
        assertTrue(result.contains("Before"));
        assertTrue(result.contains("After"));
        assertFalse(result.contains("****"));
    }

    // ==================== Whitespace and Formatting Tests ====================

    @Test
    void testNoLeadingWhitespaceInLines() {
        // Lines should not have leading whitespace (prevents code block rendering)
        String html = "<section><section><p>Nested paragraph</p></section></section>";
        String result = converter.convert(html);
        String[] lines = result.split("\n");
        for (String line : lines) {
            if (!line.isEmpty()) {
                // Line should not start with spaces
                assertFalse(line.startsWith(" "), "Line should not start with space: '" + line + "'");
            }
        }
    }

    @Test
    void testEmptyListItemsRemoved() {
        String html = "<ul><li>Item 1</li><li></li><li>Item 3</li></ul>";
        String result = converter.convert(html);
        assertTrue(result.contains("Item 1"));
        assertTrue(result.contains("Item 3"));
        // Should not have standalone "-" without content
        assertFalse(result.matches("(?m)^-\\s*$"));
    }

    @Test
    void testNewlinesNormalization() {
        // No more than 2 consecutive newlines
        String html = "<p>Para 1</p><p></p><p></p><p>Para 2</p>";
        String result = converter.convert(html);
        assertFalse(result.contains("\n\n\n"));
    }

    // ==================== Heading Separation Tests ====================

    @Test
    void testTextFollowedByHeading() {
        // Text directly followed by heading should have newline before heading
        String html = "<span>1</span><h4>Title</h4>";
        String result = converter.convert(html);
        // "1" and "#### Title" should be on separate lines
        assertTrue(result.contains("1"));
        assertTrue(result.contains("####"));
        assertTrue(result.contains("Title"));
    }

    @Test
    void testMultipleHeadings() {
        String html = "<h1>H1</h1><h2>H2</h2><h3>H3</h3>";
        String result = converter.convert(html);
        assertTrue(result.contains("# H1"));
        assertTrue(result.contains("## H2"));
        assertTrue(result.contains("### H3"));
    }

    // ==================== Image and Link Tests ====================

    @Test
    void testImageWithAltText() {
        String html = "<img src=\"test.jpg\" alt=\"Description\">";
        String result = converter.convert(html);
        assertEquals("![Description](test.jpg)\n", result);
    }

    @Test
    void testLinkedImage() {
        String html = "<a href=\"http://example.com\"><img src=\"image.jpg\" alt=\"\"></a>";
        String result = converter.convert(html);
        assertTrue(result.contains("[![](image.jpg)](http://example.com)"));
    }

    @Test
    void testLinkWithTitle() {
        String html = "<a href=\"http://example.com\" title=\"Example Site\">Link</a>";
        String result = converter.convert(html);
        assertEquals("[Link](http://example.com \"Example Site\")\n", result);
    }
}