package com.github.htmltomd.handler.impl;

import com.github.htmltomd.handler.ElementHandler;
import com.github.htmltomd.handler.HandlerContext;
import org.jsoup.nodes.Element;

/**
 * Handles conversion of link elements to Markdown.
 */
public class LinkHandler implements ElementHandler {

    @Override
    public boolean canHandle(Element element) {
        return "a".equals(element.tagName().toLowerCase());
    }

    @Override
    public String handle(Element element, HandlerContext context) {
        String text = context.processChildren(element).trim();
        String href = element.attr("href");
        String title = element.attr("title");

        if (href.isEmpty()) {
            return text;
        }

        if (!title.isEmpty()) {
            return String.format("[%s](%s \"%s\")", text, href, title);
        } else {
            return String.format("[%s](%s)", text, href);
        }
    }
}
