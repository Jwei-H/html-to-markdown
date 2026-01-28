package com.github.htmltomd.handler.impl;

import com.github.htmltomd.handler.ElementHandler;
import com.github.htmltomd.handler.HandlerContext;
import org.jsoup.nodes.Element;

/**
 * Handles conversion of heading elements (h1-h6) to Markdown.
 */
public class HeadingHandler implements ElementHandler {

    @Override
    public boolean canHandle(Element element) {
        String tag = element.tagName().toLowerCase();
        return tag.matches("h[1-6]");
    }

    @Override
    public String handle(Element element, HandlerContext context) {
        String tag = element.tagName().toLowerCase();
        int level = Integer.parseInt(tag.substring(1));

        String prefix = "#".repeat(level);
        String text = context.processChildren(element).trim();

        return prefix + " " + text + "\n\n";
    }
}
