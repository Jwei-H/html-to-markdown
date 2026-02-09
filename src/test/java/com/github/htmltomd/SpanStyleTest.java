package com.github.htmltomd;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SpanStyleTest {

    private final HtmlToMarkdownConverter converter = new HtmlToMarkdownConverter();

    @Test
    void testSpanWithBoldStyle() {
        String html = "<span style=\"font-weight: bold;\">This should be bold</span>";
        String result = converter.convert(html);
        // With the fix, it should be bold
        assertEquals("**This should be bold**", result.trim());
    }

    @Test
    void testSpanWithItalicStyle() {
        String html = "<span style=\"font-style: italic;\">This should be italic</span>";
        String result = converter.convert(html);
        assertEquals("*This should be italic*", result.trim());
    }
}
