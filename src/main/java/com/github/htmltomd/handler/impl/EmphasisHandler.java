package com.github.htmltomd.handler.impl;

import com.github.htmltomd.handler.ElementHandler;
import com.github.htmltomd.handler.HandlerContext;
import org.jsoup.nodes.Element;

import java.util.regex.Pattern;

/**
 * Handles conversion of emphasis elements to Markdown.
 * Supports: strong/b (bold), em/i (italic), del/s (strikethrough).
 */
public class EmphasisHandler implements ElementHandler {

    // Precompiled patterns for performance
    private static final Pattern MULTIPLE_ASTERISKS = Pattern.compile("\\*{4,}");
    private static final Pattern MULTIPLE_TILDES = Pattern.compile("~{4,}");

    @Override
    public boolean canHandle(Element element) {
        String tag = element.tagName().toLowerCase();
        return tag.equals("strong") || tag.equals("b") ||
                tag.equals("em") || tag.equals("i") ||
                tag.equals("del") || tag.equals("s");
    }

    @Override
    public String handle(Element element, HandlerContext context) {
        String tag = element.tagName().toLowerCase();
        String text = context.processChildren(element);

        String result = switch (tag) {
            case "strong", "b" -> "**" + text + "**";
            case "em", "i" -> "*" + text + "*";
            case "del", "s" -> "~~" + text + "~~";
            default -> text;
        };

        // Deduplicate consecutive emphasis markers
        return deduplicateMarkers(result);
    }

    /**
     * Removes duplicate consecutive emphasis markers.
     * E.g., "******text******" -> "**text**"
     */
    private String deduplicateMarkers(String text) {
        // Remove duplicate ** (bold)
        text = MULTIPLE_ASTERISKS.matcher(text).replaceAll("**");
        // Remove duplicate ~ (strikethrough)
        text = MULTIPLE_TILDES.matcher(text).replaceAll("~~");
        // Handle edge case of *** which could be from nested bold+italic
        // Keep it as is since it's valid markdown
        return text;
    }
}
