package com.github.htmltomd.handler.impl;

import com.github.htmltomd.handler.ElementHandler;
import com.github.htmltomd.handler.HandlerContext;
import org.jsoup.nodes.Element;

/**
 * Handles conversion of horizontal rule elements to Markdown.
 */
public class HorizontalRuleHandler implements ElementHandler {

    @Override
    public boolean canHandle(Element element) {
        return "hr".equals(element.tagName().toLowerCase());
    }

    @Override
    public String handle(Element element, HandlerContext context) {
        return "---\n\n";
    }
}
