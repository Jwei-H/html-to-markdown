package com.github.htmltomd.handler.impl;

import com.github.htmltomd.handler.ElementHandler;
import com.github.htmltomd.handler.HandlerContext;
import org.jsoup.nodes.Element;

/**
 * Handles conversion of heading elements (h1-h6) to Markdown.
 */
public class HeadingHandler implements ElementHandler {

    @Override
    public String handle(Element element, HandlerContext context) {
        String tag = element.tagName().toLowerCase();
        int level = Integer.parseInt(tag.substring(1));

        String prefix = "#".repeat(level);
        String text = context.processChildren(element).trim();

        // Ensure proper spacing: newline before, space after #, newline after
        return "\n" + prefix + " " + text + "\n\n";
    }
}
