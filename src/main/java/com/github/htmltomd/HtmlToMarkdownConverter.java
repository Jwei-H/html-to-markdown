package com.github.htmltomd;

import com.github.htmltomd.handler.ElementHandler;
import com.github.htmltomd.handler.HandlerContext;
import com.github.htmltomd.handler.impl.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Main converter class for converting HTML to Markdown.
 */
public class HtmlToMarkdownConverter {

    // Precompiled regex patterns for performance
    private static final Pattern FENCED_CODE_BLOCK = Pattern.compile("```[\\s\\S]*?```", Pattern.MULTILINE);

    private final Map<String, ElementHandler> handlers;
    private final ConverterConfig config;

    public HtmlToMarkdownConverter() {
        this(ConverterConfig.defaultConfig());
    }

    public HtmlToMarkdownConverter(ConverterConfig config) {
        this.config = config;
        this.handlers = createHandlerMap();
    }

    public String convert(String html) {
        if (html == null || html.trim().isEmpty()) {
            return "";
        }

        // Step 1: Protect code blocks (both fenced and indented) only if they exist
        List<String> codeBlocks = new ArrayList<>();
        String protected_ = html;

        // Quick check: only protect if code blocks might exist
        if (html.indexOf("```") >= 0) {
            protected_ = protectCodeBlocks(html, codeBlocks);
        }

        // Step 2: Parse HTML - Jsoup wraps content in <html><body>
        Document document = Jsoup.parse(protected_);
        Element body = document.body();
        preprocess(body);
        HandlerContext context = new HandlerContext(config, handlers);

        // Step 3: Process nodes - preserve TextNodes (original Markdown), convert
        // Elements
        StringBuilder result = new StringBuilder();
        for (Node node : body.childNodes()) {
            if (node instanceof TextNode textNode) {
                // Preserve original text exactly (may be Markdown)
                String text = textNode.getWholeText();
                result.append(text);
            } else if (node instanceof Element element) {
                // Convert HTML elements to Markdown
                result.append(context.processElement(element));
            }
        }

        // Step 4: Restore code blocks
        String output = restoreCodeBlocks(result.toString(), codeBlocks);

        return cleanup(output);
    }

    /**
     * Protects code blocks from HTML processing by replacing them with
     * placeholders.
     * Handles both fenced (```) and indented (4 spaces) code blocks.
     */
    private String protectCodeBlocks(String input, List<String> codeBlocks) {
        Matcher matcher = FENCED_CODE_BLOCK.matcher(input);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            codeBlocks.add(matcher.group());
            matcher.appendReplacement(sb, "___CODE_BLOCK_" + (codeBlocks.size() - 1) + "___");
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    /**
     * Restores protected code blocks from placeholders.
     */
    private String restoreCodeBlocks(String input, List<String> codeBlocks) {
        String result = input;
        for (int i = 0; i < codeBlocks.size(); i++) {
            result = result.replace("___CODE_BLOCK_" + i + "___", codeBlocks.get(i));
        }
        return result;
    }

    private Map<String, ElementHandler> createHandlerMap() {
        Map<String, ElementHandler> handlerMap = new HashMap<>();

        // Register default handlers
        registerHandler(handlerMap, new HeadingHandler(), "h1", "h2", "h3", "h4", "h5", "h6");
        registerHandler(handlerMap, new ParagraphHandler(), "p");
        registerHandler(handlerMap, new LinkHandler(), "a");
        registerHandler(handlerMap, new ImageHandler(), "img");
        registerHandler(handlerMap, new EmphasisHandler(), "strong", "b", "em", "i", "del", "s");
        registerHandler(handlerMap, new CodeHandler(), "code", "pre");
        registerHandler(handlerMap, new ListHandler(), "ul", "ol", "li");
        registerHandler(handlerMap, new BlockquoteHandler(), "blockquote");
        registerHandler(handlerMap, new TableHandler(), "table");
        registerHandler(handlerMap, new HorizontalRuleHandler(), "hr");
        registerHandler(handlerMap, new LineBreakHandler(), "br");
        registerHandler(handlerMap, new SpanHandler(), "span");

        // Merge custom handlers from config (they override defaults if tag names match)
        for (Map.Entry<String, ElementHandler> entry : config.getCustomHandlers().entrySet()) {
            handlerMap.put(entry.getKey(), entry.getValue());
        }

        return handlerMap;
    }

    /**
     * Helper method to register a handler for multiple tags.
     */
    private void registerHandler(Map<String, ElementHandler> map, ElementHandler handler, String... tags) {
        for (String tag : tags) {
            map.put(tag.toLowerCase(), handler);
        }
    }

    /**
     * Preprocesses the DOM to merge consecutive headings of the same level.
     * Merges:
     * 1.
     * <h1>A</h1>
     * <h1>B</h1> ->
     * <h1>A <br>
     * B</h1>
     * 2.
     * <h1>A</h1><br>
     * <h1>B</h1> ->
     * <h1>A <br>
     * B</h1>
     */
    private void preprocess(Element body) {
        for (int i = 0; i < body.childrenSize();) {
            Element current = body.child(i);

            if (!isHeading(current)) {
                i++;
                continue;
            }

            boolean merged = false;
            Element next = current.nextElementSibling();

            if (next != null) {
                // Case 1: Adjacent same-tag headings
                if (next.tagName().equals(current.tagName())) {
                    if (!hasSignificantTextBetween(current, next)) {
                        mergeHeadings(current, next);
                        next.remove();
                        merged = true;
                    }
                }
                // Case 2: Heading + BR + Same-tag Heading
                else if (next.tagName().equalsIgnoreCase("br")) {
                    Element nextNext = next.nextElementSibling();
                    if (nextNext != null && nextNext.tagName().equals(current.tagName())) {
                        if (!hasSignificantTextBetween(current, next) && !hasSignificantTextBetween(next, nextNext)) {
                            mergeHeadings(current, nextNext);
                            next.remove();
                            nextNext.remove();
                            merged = true;
                        }
                    }
                }
            }

            if (!merged) {
                i++;
            }
        }
    }

    private boolean isHeading(Element e) {
        String name = e.tagName().toLowerCase();
        return name.length() == 2 && name.charAt(0) == 'h' && Character.isDigit(name.charAt(1));
    }

    private void mergeHeadings(Element target, Element source) {
        target.appendChild(new TextNode(" <br> "));
        while (source.childNodeSize() > 0) {
            target.appendChild(source.childNode(0));
        }
    }

    private boolean hasSignificantTextBetween(Element e1, Element e2) {
        Node sibling = e1.nextSibling();
        while (sibling != null && sibling != e2) {
            if (sibling instanceof TextNode textNode) {
                if (!textNode.isBlank()) {
                    return true;
                }
            }
            sibling = sibling.nextSibling();
        }
        return false;
    }

    private String cleanup(String text) {
        // The handlers now generate well-formatted Markdown directly, so we only need
        // minimal cleanup: trim lines, preserve code blocks, and compress excessive
        // newlines

        String[] lines = text.split("\n", -1);
        StringBuilder cleaned = new StringBuilder(text.length());
        boolean inCodeBlock = false;
        int consecutiveEmptyLines = 0;

        for (String line : lines) {
            String trimmedLine = line.trim();

            if (trimmedLine.startsWith("```")) {
                // Reset empty line counter when entering/exiting code block
                consecutiveEmptyLines = 0;
                inCodeBlock = !inCodeBlock;
                cleaned.append(trimmedLine).append("\n");
            } else if (inCodeBlock) {
                // Preserve code block content exactly
                cleaned.append(line).append("\n");
            } else if (trimmedLine.isEmpty()) {
                // Track consecutive empty lines, max 1 (will result in max 2 newlines)
                if (consecutiveEmptyLines < 1) {
                    cleaned.append("\n");
                    consecutiveEmptyLines++;
                }
                // Skip additional empty lines
            } else {
                // Non-empty line outside code block
                consecutiveEmptyLines = 0;
                cleaned.append(trimmedLine).append("\n");
            }
        }

        String result = cleaned.toString();

        return result.trim() + "\n";
    }
}
