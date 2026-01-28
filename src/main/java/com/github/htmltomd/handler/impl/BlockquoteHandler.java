package com.github.htmltomd.handler.impl;

import com.github.htmltomd.handler.ElementHandler;
import com.github.htmltomd.handler.HandlerContext;
import org.jsoup.nodes.Element;

/**
 * Handles conversion of blockquote elements to Markdown.
 */
public class BlockquoteHandler implements ElementHandler {

    @Override
    public boolean canHandle(Element element) {
        return "blockquote".equals(element.tagName().toLowerCase());
    }

    @Override
    public String handle(Element element, HandlerContext context) {
        String content = context.processChildren(element).trim();

        if (content.isEmpty()) {
            return "";
        }

        // Split by lines and prefix each with >
        String[] lines = content.split("\n");
        StringBuilder result = new StringBuilder();

        for (String line : lines) {
            result.append("> ").append(line.trim()).append("\n");
        }

        result.append("\n");
        return result.toString();
    }
}
