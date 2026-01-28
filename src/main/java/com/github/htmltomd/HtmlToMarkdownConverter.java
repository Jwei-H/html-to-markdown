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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Main converter class for converting HTML to Markdown.
 */
public class HtmlToMarkdownConverter {

    // Precompiled regex patterns for performance
    private static final Pattern FENCED_CODE_BLOCK = Pattern.compile("```[\\s\\S]*?```", Pattern.MULTILINE);
    private static final Pattern MULTIPLE_NEWLINES = Pattern.compile("\n{3,}");
    private static final Pattern EMPTY_LIST_ITEM = Pattern.compile("^-\\s*$", Pattern.MULTILINE);
    private static final Pattern LIST_NEWLINE_FIX = Pattern.compile("(?<=\\n[^-\\n][^\\n]*)(\\n- )");
    private static final Pattern HEADING_NO_SPACE = Pattern.compile("([^\\s#])(#{1,6} )");
    private static final Pattern HEADING_SINGLE_NEWLINE = Pattern.compile("([^#\\n])(\\n#{1,6} )");
    private static final Pattern MULTIPLE_ASTERISKS = Pattern.compile("\\*{4,}");
    private static final Pattern MULTIPLE_TILDES = Pattern.compile("~{4,}");
    private static final Pattern ORPHANED_EMPHASIS = Pattern.compile(" +\\*\\*$", Pattern.MULTILINE);
    private static final Pattern EMPHASIS_PAIR_BOLD = Pattern.compile("\\*\\*(.*?)\\*\\*");
    private static final Pattern EMPHASIS_PAIR_STRIKE = Pattern.compile("~~(.*?)~~");

    private final List<ElementHandler> defaultHandlers;
    private final ConverterConfig config;

    public HtmlToMarkdownConverter() {
        this(ConverterConfig.defaultConfig());
    }

    public HtmlToMarkdownConverter(ConverterConfig config) {
        this.config = config;
        this.defaultHandlers = createDefaultHandlers();
    }

    public String convert(String html) {
        if (html == null || html.trim().isEmpty()) {
            return "";
        }

        // Step 1: Protect code blocks (both fenced and indented)
        List<String> codeBlocks = new ArrayList<>();
        String protected_ = protectCodeBlocks(html, codeBlocks);

        // Step 2: Parse HTML - Jsoup wraps content in <html><body>
        Document document = Jsoup.parse(protected_);
        Element body = document.body();
        HandlerContext context = new HandlerContext(config, defaultHandlers);

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

    private List<ElementHandler> createDefaultHandlers() {
        List<ElementHandler> handlers = new ArrayList<>();
        handlers.add(new HeadingHandler());
        handlers.add(new ParagraphHandler());
        handlers.add(new LinkHandler());
        handlers.add(new ImageHandler());
        handlers.add(new EmphasisHandler());
        handlers.add(new CodeHandler());
        handlers.add(new ListHandler());
        handlers.add(new BlockquoteHandler());
        handlers.add(new TableHandler());
        handlers.add(new HorizontalRuleHandler());
        handlers.add(new LineBreakHandler());
        return handlers;
    }

    private String cleanup(String text) {
        // Step 1: Trim whitespace from lines, preserve code blocks, and compress
        // newlines in one pass
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

        // Step 2: Remove empty list items
        result = EMPTY_LIST_ITEM.matcher(result).replaceAll("");

        // Step 3: Ensure list items have newline before (when previous line is not a
        // list item)
        result = LIST_NEWLINE_FIX.matcher(result).replaceAll("\n$1");

        // Step 4: Ensure headings have proper spacing
        result = HEADING_NO_SPACE.matcher(result).replaceAll("$1\n$2");
        result = HEADING_SINGLE_NEWLINE.matcher(result).replaceAll("$1\n$2");

        // Step 5: Merge adjacent emphasis markers (handles both ** and ~~)
        result = MULTIPLE_ASTERISKS.matcher(result).replaceAll("");
        result = MULTIPLE_TILDES.matcher(result).replaceAll("");

        // Step 6: Fix emphasis markers - process ** and ~~ pairs
        result = fixEmphasisPairs(result, EMPHASIS_PAIR_BOLD, "**");
        result = fixEmphasisPairs(result, EMPHASIS_PAIR_STRIKE, "~~");

        // Step 7: Clean up remaining orphaned emphasis at line end
        result = ORPHANED_EMPHASIS.matcher(result).replaceAll("");

        // Step 8: Final newline cleanup (may be needed after list/heading fixes
        // introduced new lines)
        result = MULTIPLE_NEWLINES.matcher(result).replaceAll("\n\n");

        return result.trim() + "\n";
    }

    /**
     * Fixes emphasis pairs by:
     * - Trimming spaces inside the markers (e.g., "** text **" -> "**text**")
     * - Removing empty pairs (e.g., "** **" -> "")
     */
    private String fixEmphasisPairs(String text, Pattern pattern, String marker) {
        Matcher matcher = pattern.matcher(text);

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String content = matcher.group(1);
            // Trim leading and trailing whitespace from the content
            String trimmed = content.trim();

            if (trimmed.isEmpty()) {
                // Empty pair - remove entirely
                matcher.appendReplacement(sb, "");
            } else {
                // Non-empty - replace with trimmed content
                // Escape $ and \ for replacement
                String replacement = marker + Matcher.quoteReplacement(trimmed) + marker;
                matcher.appendReplacement(sb, replacement);
            }
        }
        matcher.appendTail(sb);

        return sb.toString();
    }
}
