package com.github.htmltomd.handler.impl;

import com.github.htmltomd.handler.ElementHandler;
import com.github.htmltomd.handler.HandlerContext;
import org.jsoup.nodes.Element;

import java.util.regex.Pattern;

/**
 * Handles conversion of heading elements (h1-h6) to Markdown.
 */
public class HeadingHandler implements ElementHandler {

    private static final Pattern IMAGE_ONLY_PATTERN = Pattern.compile("^!\\[.*?\\]\\(.*?\\)$");

    @Override
    public String handle(Element element, HandlerContext context) {
        String tag = element.tagName().toLowerCase();
        int level = Integer.parseInt(tag.substring(1));

        String text = context.processChildren(element).trim();

        // Remove empty headings
        if (text.isEmpty()) {
            return "";
        }

        // If heading contains only an image, return just the image (remove heading
        // attribute)
        if (IMAGE_ONLY_PATTERN.matcher(text).matches()) {
            return text + "\n\n";
        }

        String prefix = "#".repeat(level);
        // Ensure proper spacing: newline before, space after #, newline after
        return "\n" + prefix + " " + text + "\n\n";
    }
}
