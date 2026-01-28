package com.github.htmltomd.handler.impl;

import com.github.htmltomd.handler.ElementHandler;
import com.github.htmltomd.handler.HandlerContext;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Handles conversion of list elements to Markdown.
 * Supports: ul (unordered lists), ol (ordered lists), li (list items).
 */
public class ListHandler implements ElementHandler {

    @Override
    public boolean canHandle(Element element) {
        String tag = element.tagName().toLowerCase();
        return tag.equals("ul") || tag.equals("ol") || tag.equals("li");
    }

    @Override
    public String handle(Element element, HandlerContext context) {
        String tag = element.tagName().toLowerCase();

        if (tag.equals("li")) {
            return handleListItem(element, context);
        } else {
            return handleList(element, context, tag.equals("ol"));
        }
    }

    private String handleList(Element element, HandlerContext context, boolean ordered) {
        StringBuilder result = new StringBuilder();
        Elements items = element.children();

        int index = 1;
        for (Element item : items) {
            if ("li".equals(item.tagName().toLowerCase())) {
                String marker = ordered ? (index++ + ".") : "-";
                String indent = "  ".repeat(context.getIndentLevel());

                // Process list item content
                context.increaseIndent();
                String content = context.processElement(item).trim();
                context.decreaseIndent();

                // Handle nested lists
                if (content.contains("\n")) {
                    String[] lines = content.split("\n");
                    result.append(indent).append(marker).append(" ").append(lines[0]).append("\n");
                    for (int i = 1; i < lines.length; i++) {
                        result.append(indent).append("  ").append(lines[i]).append("\n");
                    }
                } else {
                    result.append(indent).append(marker).append(" ").append(content).append("\n");
                }
            }
        }

        // Add blank line after list if at top level
        if (context.getIndentLevel() == 0) {
            result.append("\n");
        }

        return result.toString();
    }

    private String handleListItem(Element element, HandlerContext context) {
        StringBuilder result = new StringBuilder();

        for (org.jsoup.nodes.Node node : element.childNodes()) {
            if (node instanceof org.jsoup.nodes.TextNode textNode) {
                result.append(textNode.text());
            } else if (node instanceof Element childElement) {
                String tag = childElement.tagName().toLowerCase();
                // Handle nested lists specially
                if (tag.equals("ul") || tag.equals("ol")) {
                    result.append("\n").append(context.processElement(childElement));
                } else {
                    result.append(context.processElement(childElement));
                }
            }
        }

        return result.toString();
    }
}
