package com.github.htmltomd.handler.impl;

import com.github.htmltomd.handler.ElementHandler;
import com.github.htmltomd.handler.HandlerContext;
import org.jsoup.nodes.Element;

/**
 * Handles conversion of line break elements to Markdown.
 */
public class LineBreakHandler implements ElementHandler {

    @Override
    public boolean canHandle(Element element) {
        return "br".equals(element.tagName().toLowerCase());
    }

    @Override
    public String handle(Element element, HandlerContext context) {
        return "  \n";
    }
}
