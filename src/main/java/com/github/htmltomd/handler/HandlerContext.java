package com.github.htmltomd.handler;

import com.github.htmltomd.ConverterConfig;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Context for element conversion providing access to configuration and helper
 * methods.
 */
public class HandlerContext {

    // Precompiled patterns for whitespace normalization
    private static final Pattern MULTIPLE_SPACES = Pattern.compile(" {2,}");
    private static final Pattern LEADING_WHITESPACE = Pattern.compile("^\\s+");

    private final ConverterConfig config;
    private final Map<String, ElementHandler> handlers;
    private int indentLevel = 0;

    public HandlerContext(ConverterConfig config, Map<String, ElementHandler> handlers) {
        this.config = config;
        this.handlers = handlers;
    }

    /**
     * Gets the configuration.
     * 
     * @return the configuration
     */
    public ConverterConfig getConfig() {
        return config;
    }

    /**
     * Gets current indentation level.
     * 
     * @return the indentation level
     */
    public int getIndentLevel() {
        return indentLevel;
    }

    /**
     * Increases indentation level.
     */
    public void increaseIndent() {
        indentLevel++;
    }

    /**
     * Decreases indentation level.
     */
    public void decreaseIndent() {
        if (indentLevel > 0) {
            indentLevel--;
        }
    }

    /**
     * Processes child elements of the given element.
     * 
     * @param element the parent element
     * @return the Markdown representation of children
     */
    public String processChildren(Element element) {
        StringBuilder result = new StringBuilder();

        for (Node node : element.childNodes()) {
            if (node instanceof TextNode textNode) {
                String text = textNode.text();
                // Normalize whitespace in text nodes
                text = normalizeTextWhitespace(text, result.length() == 0);
                result.append(text);
            } else if (node instanceof Element childElement) {
                result.append(processElement(childElement));
            }
        }

        return result.toString();
    }

    /**
     * Normalizes whitespace in text content.
     * - Trims leading whitespace if at the start of output
     * - Preserves single spaces between words
     * - Removes excessive whitespace
     */
    private String normalizeTextWhitespace(String text, boolean isStart) {
        if (text.isEmpty()) {
            return text;
        }

        // Collapse multiple spaces to single space
        text = MULTIPLE_SPACES.matcher(text).replaceAll(" ");

        // Trim leading whitespace if this is the start
        if (isStart) {
            text = LEADING_WHITESPACE.matcher(text).replaceAll("");
        }

        return text;
    }

    /**
     * Processes a single element.
     * 
     * @param element the element to process
     * @return the Markdown representation
     */
    public String processElement(Element element) {
        String tagName = element.tagName().toLowerCase();

        // Check if should preserve as HTML
        if (config.shouldPreserveTag(tagName)) {
            return element.outerHtml();
        }

        // Check if should remove completely
        if (config.shouldRemoveTag(tagName)) {
            return "";
        }

        // Try custom handler first (from config)
        ElementHandler customHandler = config.getCustomHandler(tagName);
        if (customHandler != null) {
            return customHandler.handle(element, this);
        }

        // Try default handler from map (O(1) lookup)
        ElementHandler handler = handlers.get(tagName);
        if (handler != null) {
            return handler.handle(element, this);
        }

        // Default: just process children
        return processChildren(element);
    }
}
