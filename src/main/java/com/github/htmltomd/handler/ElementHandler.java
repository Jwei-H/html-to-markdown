package com.github.htmltomd.handler;

import org.jsoup.nodes.Element;

/**
 * Interface for handling conversion of specific HTML elements to Markdown.
 * This is a functional interface, allowing for lambda expressions in custom
 * handlers.
 */
@FunctionalInterface
public interface ElementHandler {

    /**
     * Converts the HTML element to Markdown.
     * 
     * @param element the HTML element
     * @param context the conversion context
     * @return the Markdown representation
     */
    String handle(Element element, HandlerContext context);

    /**
     * Checks if this handler can handle the given element.
     * Default implementation returns true, as tag-based routing is handled by the
     * Map dispatcher.
     * Override this method only if you need attribute-based or complex conditional
     * routing.
     * 
     * @param element the HTML element
     * @return true if this handler can handle the element
     */
    default boolean canHandle(Element element) {
        return true;
    }
}
