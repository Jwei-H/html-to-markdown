package com.github.htmltomd.handler;

import org.jsoup.nodes.Element;

/**
 * Interface for handling conversion of specific HTML elements to Markdown.
 */
public interface ElementHandler {

    /**
     * Checks if this handler can handle the given element.
     * 
     * @param element the HTML element
     * @return true if this handler can handle the element
     */
    boolean canHandle(Element element);

    /**
     * Converts the HTML element to Markdown.
     * 
     * @param element the HTML element
     * @param context the conversion context
     * @return the Markdown representation
     */
    String handle(Element element, HandlerContext context);
}
