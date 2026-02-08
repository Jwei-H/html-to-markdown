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

        // Add newline before top-level lists to separate from previous content
        if (context.getIndentLevel() == 0) {
            result.append("\n");
        }

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

                // Skip empty list items
                if (content.isEmpty()) {
                    continue;
                }

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
        boolean firstElement = true;

        for (org.jsoup.nodes.Node node : element.childNodes()) {
            if (node instanceof org.jsoup.nodes.TextNode textNode) {
                String text = textNode.text().trim();
                if (!text.isEmpty()) {
                    if (result.length() > 0 && !result.toString().endsWith("\n")) {
                        result.append(" ");
                    }
                    result.append(text);
                    firstElement = false;
                }
            } else if (node instanceof Element childElement) {
                String tag = childElement.tagName().toLowerCase();
                // Handle nested lists specially
                if (tag.equals("ul") || tag.equals("ol")) {
                    result.append("\n").append(context.processElement(childElement));
                } else if (tag.equals("p") || tag.equals("section")) {
                    // For paragraphs/sections in list items, add line breaks between them
                    String pContent = context.processElement(childElement).trim();
                    if (!pContent.isEmpty()) {
                        if (!firstElement && result.length() > 0) {
                            // Add double space + newline for proper line break in Markdown list
                            result.append("  \n");
                        }
                        result.append(pContent);
                        firstElement = false;
                    }
                } else {
                    String content = context.processElement(childElement);
                    if (!content.trim().isEmpty()) {
                        result.append(content);
                        firstElement = false;
                    }
                }
            }
        }

        return result.toString();
    }
}
