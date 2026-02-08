package com.github.htmltomd.handler.impl;

import com.github.htmltomd.ConverterConfig;
import com.github.htmltomd.handler.HandlerContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for optimization improvements.
 */
class OptimizationTest {

    @Test
    void testTableWithEmptyRows() {
        TableHandler handler = new TableHandler();
        Map<String, com.github.htmltomd.handler.ElementHandler> handlers = new HashMap<>();
        handlers.put("table", handler);
        HandlerContext context = new HandlerContext(ConverterConfig.defaultConfig(), handlers);

        String html = "<table>" +
                "<tr><th>Header 1</th><th>Header 2</th></tr>" +
                "<tr><td>Data 1</td><td>Data 2</td></tr>" +
                "<tr><td></td><td></td></tr>" + // Empty row should be filtered
                "<tr><td>Data 3</td><td>Data 4</td></tr>" +
                "</table>";

        Element table = Jsoup.parse(html).body().child(0);
        String result = handler.handle(table, context);

        // Should not contain empty row
        assertFalse(result.contains("|  |  |"));
        // Should contain header and two data rows
        assertTrue(result.contains("Header 1"));
        assertTrue(result.contains("Data 1"));
        assertTrue(result.contains("Data 3"));
        // Count rows: header + separator + 2 data rows = 4 lines total
        // But formatTable adds extra newline, so we get 5 lines when split
        String[] lines = result.split("\n");
        // Filter out empty strings
        long nonEmptyLines = java.util.Arrays.stream(lines).filter(s -> !s.isEmpty()).count();
        assertEquals(4, nonEmptyLines); // header + separator + 2 data rows
    }

    @Test
    void testNestedEmphasis() {
        EmphasisHandler handler = new EmphasisHandler();
        Map<String, com.github.htmltomd.handler.ElementHandler> handlers = new HashMap<>();
        handlers.put("strong", handler);
        handlers.put("span", (element, ctx) -> ctx.processChildren(element));
        HandlerContext context = new HandlerContext(ConverterConfig.defaultConfig(), handlers);

        // Nested strong tags
        String html = "<strong><strong><span>本文</span><strong><span>首发于</span></strong></strong></strong>";
        Element root = Jsoup.parse(html).body().child(0);
        String result = handler.handle(root, context);

        assertFalse(result.contains("******"));
        assertTrue(result.contains("**"));
    }

    @Test
    void testComplexNestedEmphasis() {
        EmphasisHandler handler = new EmphasisHandler();
        Map<String, com.github.htmltomd.handler.ElementHandler> handlers = new HashMap<>();
        handlers.put("strong", handler);
        HandlerContext context = new HandlerContext(ConverterConfig.defaultConfig(), handlers);

        // Very deeply nested
        String html = "<strong><strong><strong><strong>text</strong></strong></strong></strong>";
        Element root = Jsoup.parse(html).body().child(0);
        String result = handler.handle(root, context);

        // Should become just **text**
        assertEquals("**text**", result);
    }
}