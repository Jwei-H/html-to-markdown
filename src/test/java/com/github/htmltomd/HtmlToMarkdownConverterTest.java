package com.github.htmltomd;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive integration tests for HtmlToMarkdownConverter.
 */
class HtmlToMarkdownConverterTest {

    private final HtmlToMarkdownConverter converter = new HtmlToMarkdownConverter();

    @Test
    void testEmptyString() {
        assertEquals("", converter.convert(""));
        assertEquals("", converter.convert(null));
        assertEquals("", converter.convert("   "));
    }

    @Test
    void testHeadings() {
        assertEquals("# Heading 1\n", converter.convert("<h1>Heading 1</h1>"));
        assertEquals("## Heading 2\n", converter.convert("<h2>Heading 2</h2>"));
        assertEquals("### Heading 3\n", converter.convert("<h3>Heading 3</h3>"));
        assertEquals("#### Heading 4\n", converter.convert("<h4>Heading 4</h4>"));
        assertEquals("##### Heading 5\n", converter.convert("<h5>Heading 5</h5>"));
        assertEquals("###### Heading 6\n", converter.convert("<h6>Heading 6</h6>"));
    }

    @Test
    void testParagraphs() {
        String html = "<p>First paragraph.</p><p>Second paragraph.</p>";
        String expected = "First paragraph.\n\nSecond paragraph.\n";
        assertEquals(expected, converter.convert(html));
    }

    @Test
    void testEmphasis() {
        assertEquals("**bold**\n", converter.convert("<strong>bold</strong>"));
        assertEquals("**bold**\n", converter.convert("<b>bold</b>"));
        assertEquals("*italic*\n", converter.convert("<em>italic</em>"));
        assertEquals("*italic*\n", converter.convert("<i>italic</i>"));
        assertEquals("~~strikethrough~~\n", converter.convert("<del>strikethrough</del>"));
        assertEquals("~~strikethrough~~\n", converter.convert("<s>strikethrough</s>"));
    }

    @Test
    void testNestedEmphasis() {
        String html = "<p>This is <strong>bold and <em>italic</em></strong> text.</p>";
        String expected = "This is **bold and *italic*** text.\n";
        assertEquals(expected, converter.convert(html));
    }

    @Test
    void testLinks() {
        assertEquals("[link](http://example.com)\n",
                converter.convert("<a href=\"http://example.com\">link</a>"));

        assertEquals("[link](http://example.com \"title\")\n",
                converter.convert("<a href=\"http://example.com\" title=\"title\">link</a>"));
    }

    @Test
    void testImages() {
        assertEquals("![alt text](image.jpg)\n",
                converter.convert("<img src=\"image.jpg\" alt=\"alt text\">"));

        assertEquals("![alt text](image.jpg \"title\")\n",
                converter.convert("<img src=\"image.jpg\" alt=\"alt text\" title=\"title\">"));
    }

    @Test
    void testInlineCode() {
        String html = "<p>Use <code>System.out.println()</code> to print.</p>";
        String expected = "Use `System.out.println()` to print.\n";
        assertEquals(expected, converter.convert(html));
    }

    @Test
    void testCodeBlock() {
        String html = "<pre><code>public static void main(String[] args) {\n    System.out.println(\"Hello\");\n}</code></pre>";
        String expected = "```\npublic static void main(String[] args) {\n    System.out.println(\"Hello\");\n}\n```\n";
        assertEquals(expected, converter.convert(html));
    }

    @Test
    void testCodeBlockWithLanguage() {
        String html = "<pre><code class=\"language-java\">System.out.println(\"Hello\");</code></pre>";
        String expected = "```java\nSystem.out.println(\"Hello\");\n```\n";
        assertEquals(expected, converter.convert(html));
    }

    @Test
    void testUnorderedList() {
        String html = "<ul><li>Item 1</li><li>Item 2</li><li>Item 3</li></ul>";
        String expected = "- Item 1\n- Item 2\n- Item 3\n";
        assertEquals(expected, converter.convert(html));
    }

    @Test
    void testOrderedList() {
        String html = "<ol><li>First</li><li>Second</li><li>Third</li></ol>";
        String expected = "1. First\n2. Second\n3. Third\n";
        assertEquals(expected, converter.convert(html));
    }

    @Test
    void testNestedList() {
        String html = "<ul><li>Item 1<ul><li>Nested 1</li><li>Nested 2</li></ul></li><li>Item 2</li></ul>";
        String result = converter.convert(html);
        assertTrue(result.contains("Item 1"));
        assertTrue(result.contains("Nested 1"));
        assertTrue(result.contains("Nested 2"));
        assertTrue(result.contains("Item 2"));
    }

    @Test
    void testBlockquote() {
        String html = "<blockquote>This is a quote</blockquote>";
        String expected = "> This is a quote\n";
        assertEquals(expected, converter.convert(html));
    }

    @Test
    void testMultiLineBlockquote() {
        String html = "<blockquote><p>Line 1</p><p>Line 2</p></blockquote>";
        String result = converter.convert(html);
        assertTrue(result.contains("> Line 1"));
        assertTrue(result.contains("> Line 2"));
    }

    @Test
    void testTable() {
        String html = "<table>" +
                "<thead><tr><th>Header 1</th><th>Header 2</th></tr></thead>" +
                "<tbody><tr><td>Cell 1</td><td>Cell 2</td></tr></tbody>" +
                "</table>";

        String result = converter.convert(html);
        assertTrue(result.contains("| Header 1 | Header 2 |"));
        assertTrue(result.contains("|-------|-------|"));
        assertTrue(result.contains("| Cell 1 | Cell 2 |"));
    }

    @Test
    void testHorizontalRule() {
        String html = "<p>Before</p><hr><p>After</p>";
        String result = converter.convert(html);
        assertTrue(result.contains("Before"));
        assertTrue(result.contains("---"));
        assertTrue(result.contains("After"));
    }

    @Test
    void testComplexDocument() {
        String html = "<h1>Main Title</h1>" +
                "<p>This is a <strong>complex</strong> document with <a href=\"http://example.com\">links</a>.</p>" +
                "<h2>Section 1</h2>" +
                "<ul><li>Item 1</li><li>Item 2</li></ul>" +
                "<pre><code>code block</code></pre>";

        String result = converter.convert(html);
        assertTrue(result.contains("# Main Title"));
        assertTrue(result.contains("**complex**"));
        assertTrue(result.contains("[links](http://example.com)"));
        assertTrue(result.contains("## Section 1"));
        assertTrue(result.contains("- Item 1"));
        assertTrue(result.contains("```"));
    }

    @Test
    void testCustomConfiguration() {
        ConverterConfig config = ConverterConfig.builder()
                .preserveTag("sup")
                .build();

        String html = "<p>E=mc<sup>2</sup></p>";
        String result = new HtmlToMarkdownConverter(config).convert(html);
        assertTrue(result.contains("<sup>2</sup>"));
    }

    @Test
    void testRemoveTag() {
        ConverterConfig config = ConverterConfig.builder()
                .removeTag("script")
                .build();

        String html = "<p>Content</p><script>alert('test');</script>";
        String result = new HtmlToMarkdownConverter(config).convert(html);
        assertTrue(result.contains("Content"));
        assertFalse(result.contains("script"));
        assertFalse(result.contains("alert"));
    }

    @Test
    void testLineBreak() {
        String html = "<p>Line 1<br>Line 2</p>";
        String result = converter.convert(html);
        assertTrue(result.contains("Line 1"));
        assertTrue(result.contains("Line 2"));
    }
}
