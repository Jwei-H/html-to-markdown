package com.github.htmltomd.handler.impl;

import com.github.htmltomd.handler.ElementHandler;
import com.github.htmltomd.handler.HandlerContext;
import org.jsoup.nodes.Element;

/**
 * Handles conversion of image elements to Markdown.
 */
public class ImageHandler implements ElementHandler {

    @Override
    public boolean canHandle(Element element) {
        return "img".equals(element.tagName().toLowerCase());
    }

    @Override
    public String handle(Element element, HandlerContext context) {
        String src = element.attr("src");
        String alt = element.attr("alt");
        String title = element.attr("title");

        if (src.isEmpty()) {
            return "";
        }

        if (!title.isEmpty()) {
            return String.format("![%s](%s \"%s\")", alt, src, title);
        } else {
            return String.format("![%s](%s)", alt, src);
        }
    }
}
