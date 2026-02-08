package com.github.htmltomd.handler.impl;

import com.github.htmltomd.handler.ElementHandler;
import com.github.htmltomd.handler.HandlerContext;
import org.jsoup.nodes.Element;

/**
 * Handles conversion of emphasis elements to Markdown.
 * Supports: strong/b (bold), em/i (italic), del/s (strikethrough).
 */
public class EmphasisHandler implements ElementHandler {

    @Override
    public String handle(Element element, HandlerContext context) {
        String tag = element.tagName().toLowerCase();
        String text = context.processChildren(element).trim(); // Trim to avoid spaces inside markers

        // If content is empty after trimming, don't generate markers
        if (text.isEmpty()) {
            return "";
        }

        // Check if content already has the same emphasis markers to avoid duplication
        // This handles nested emphasis tags like <strong><strong>text</strong></strong>
        String marker = switch (tag) {
            case "strong", "b" -> "**";
            case "em", "i" -> "*";
            case "del", "s" -> "~~";
            default -> "";
        };

        // If the marker is not empty and content already starts/ends with this marker,
        // skip adding
        if (!marker.isEmpty() && text.startsWith(marker) && text.endsWith(marker)
                && text.length() > marker.length() * 2) {
            return text; // Already has the same markers, don't duplicate
        }

        return switch (tag) {
            case "strong", "b" -> "**" + text + "**";
            case "em", "i" -> "*" + text + "*";
            case "del", "s" -> "~~" + text + "~~";
            default -> text;
        };
    }
}
