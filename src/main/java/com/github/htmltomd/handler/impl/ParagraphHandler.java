package com.github.htmltomd.handler.impl;

import com.github.htmltomd.handler.ElementHandler;
import com.github.htmltomd.handler.HandlerContext;
import org.jsoup.nodes.Element;

/**
 * Handles conversion of paragraph elements to Markdown.
 */
public class ParagraphHandler implements ElementHandler {

    @Override
    public String handle(Element element, HandlerContext context) {
        String text = context.processChildren(element).trim();
        if (text.isEmpty()) {
            return "";
        }
        return text + "\n\n";
    }
}
