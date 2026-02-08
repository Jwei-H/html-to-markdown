package com.github.htmltomd.handler.impl;

import com.github.htmltomd.handler.ElementHandler;
import com.github.htmltomd.handler.HandlerContext;
import org.jsoup.nodes.Element;

/**
 * Handles conversion of code elements to Markdown.
 * Supports: code (inline), pre (code blocks).
 */
public class CodeHandler implements ElementHandler {

    @Override
    public String handle(Element element, HandlerContext context) {
        String tag = element.tagName().toLowerCase();

        if (tag.equals("pre")) {
            return handlePreTag(element, context);
        } else {
            return handleCodeTag(element);
        }
    }

    private String handleCodeTag(Element element) {
        String text = element.text();
        return "`" + text + "`";
    }

    private String handlePreTag(Element element, HandlerContext context) {
        // Check if pre contains a code element
        Element codeElement = element.selectFirst("code");
        String code;
        String language = "";

        if (codeElement != null) {
            code = codeElement.text();
            language = extractLanguage(codeElement);
        } else {
            code = element.text();
        }

        return "```" + language + "\n" + code + "\n```\n\n";
    }

    /**
     * Extracts language from code element.
     * Priority: lang attribute > class attribute (language-* or lang-*)
     */
    private String extractLanguage(Element codeElement) {
        // First, check lang attribute
        String langAttr = codeElement.attr("lang");
        if (!langAttr.isEmpty()) {
            return langAttr;
        }

        // Second, check class attribute for language- or lang- prefix
        String classAttr = codeElement.attr("class");
        if (!classAttr.isEmpty()) {
            // Split by whitespace to handle multiple classes like "hljs language-json"
            String[] classes = classAttr.split("\\s+");
            for (String cls : classes) {
                if (cls.startsWith("language-")) {
                    return cls.substring("language-".length());
                } else if (cls.startsWith("lang-")) {
                    return cls.substring("lang-".length());
                }
            }
        }

        return "";
    }
}
