package com.github.htmltomd.handler.impl;

import com.github.htmltomd.handler.ElementHandler;
import com.github.htmltomd.handler.HandlerContext;
import org.jsoup.nodes.Element;

/**
 * Handles conversion of span elements with specific styles to Markdown.
 * Supports: font-weight: bold (-> **), font-style: italic (-> *),
 * text-decoration: line-through (-> ~~).
 */
public class SpanHandler implements ElementHandler {

    @Override
    public String handle(Element element, HandlerContext context) {
        // If no style attribute, just process children without trimming
        // This is crucial to preserve newlines if the span contains block elements
        String style = element.attr("style");
        if (style == null || style.isEmpty()) {
            return context.processChildren(element);
        }

        String text = context.processChildren(element).trim();

        // If empty content, return empty
        if (text.isEmpty()) {
            return text;
        }

        boolean isBold = false;
        boolean isItalic = false;
        boolean isStrike = false;

        String[] styles = style.split(";");
        for (String s : styles) {
            String[] parts = s.split(":");
            if (parts.length < 2)
                continue;

            String key = parts[0].trim().toLowerCase();
            String value = parts[1].trim().toLowerCase();

            if (key.equals("font-weight") && (value.equals("bold") || value.equals("700") || value.equals("800")
                    || value.equals("900") || value.equals("bolder"))) {
                isBold = true;
            } else if (key.equals("font-style") && value.equals("italic")) {
                isItalic = true;
            } else if (key.equals("text-decoration") && value.contains("line-through")) {
                isStrike = true;
            }
        }

        String result = text;
        // Apply wrappers. Order: Bold -> Italic -> Strike (creates ~~*__text__*~~)
        // Standard markdown nesting is somewhat flexible, but typically bold out most
        // makes sense or vice versa.
        // Let's do Strike -> Italic -> Bold to get ***~~text~~*** if all present?
        // Actually emphasis handler does: **text**, *text*, ~~text~~.

        if (isBold) {
            result = wrap(result, "**");
        }
        if (isItalic) {
            result = wrap(result, "*");
        }
        if (isStrike) {
            result = wrap(result, "~~");
        }

        return result;
    }

    private String wrap(String text, String marker) {
        // Simple check to avoid redundant wrapping if the content is EXACTLY the same
        // wrapped content
        // e.g. text is "**foo**" and we want to wrap with "**" -> return "**foo**"
        // But if text is "**foo** bar **baz**", we SHOULD wrap -> "****foo** bar
        // **baz****" (which is valid but maybe not what we want, user implies span
        // replaces the tag)
        // If the span effectively acts as the bold tag, we should just wrap.

        // Reusing the logic from EmphasisHandler which tries to avoid duplication
        if (text.startsWith(marker) && text.endsWith(marker) && text.length() >= marker.length() * 2) {
            // Check if it's likely a single block.
            // This is a heuristic.
            return text;
        }
        return marker + text + marker;
    }
}
