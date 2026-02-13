package com.github.htmltomd;

import com.github.htmltomd.handler.ElementHandler;
import com.github.htmltomd.handler.HandlerContext;
import com.github.htmltomd.handler.impl.ParagraphHandler;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CustomRulesTest {

    @Disabled("Missing test resources (cases/*.html)")
    @Test
    public void testCustomRules() throws IOException {
        // Defines the custom handler logic
        ElementHandler customParagraphHandler = new ElementHandler() {
            private final ParagraphHandler defaultHandler = new ParagraphHandler();

            @Override
            public String handle(Element element, HandlerContext context) {
                String className = element.className();
                String style = element.attr("style");
                String text = context.processChildren(element).trim(); // Get processed text for inspection

                // Case 1: Custom Classes
                if (className.contains("text-big-title")) {
                    return "# " + text + "\n\n";
                }
                if (className.contains("text-sm-title")) {
                    return "### " + text + "\n\n";
                }

                // Case 3: Centered and Bold -> Treat as Title
                // The structure text might be complex (e.g. **text**), so just checking raw
                // text isn't enough for structural check
                // But let's check styles.
                boolean isCentered = style.contains("text-align: center");
                boolean hasBold = !element.select("span[style*='font-weight: bold']").isEmpty()
                        || !element.select("strong").isEmpty()
                        || !element.select("b").isEmpty();

                if (isCentered && hasBold) {
                    return "# " + text + "\n\n";
                }

                // Case 2: Heuristic for "First level" titles like "一、..."
                // The processed text should start with "一、" or similar.
                // Note: processChildren might return "**一、...**" if it was bolded by
                // SpanHandler/EmphasisHandler.
                // So we should strip markdown markers loosely for the check.
                String plainText = text.replace("*", "").trim();
                if (plainText.matches("^[一二三四五]、.*") && (plainText.length() < 50)) {
                    // This is a simple heuristic: if it looks like a title and is short
                    return "## " + text + "\n\n";
                }

                // Default behavior
                return defaultHandler.handle(element, context);
            }
        };

        // Create config with custom handler
        ConverterConfig config = ConverterConfig.builder()
                .addCustomHandler("p", customParagraphHandler)
                .build();

        HtmlToMarkdownConverter converter = new HtmlToMarkdownConverter(config);

        // Test Case 1
        String case1 = Files.readString(Paths.get("cases/case1.html"));
        String result1 = converter.convert(case1);
        System.out.println("--- Case 1 Result ---");
        System.out.println(result1);
        assertTrue(result1.contains("# 一、当日行情分析"));
        assertTrue(result1.contains("### 1、市场观察"));

        // Test Case 2
        // Case 2 is tricky because the text "一、..." is deep inside a div structure, and
        // the P holding it is inside that.
        // The custom handler is on 'p'.
        // The outer divs are just containers.
        // <p><span ...><strong><p><span>一、...</span></p></strong></span></p> <-- Wait,
        // the html in case2 has nested P?
        // Let's re-read case2 roughly.
        // It has `p` holding `span` holding `strong` holding `p` holding `span`.
        // Browsers/Jsoup might flatten `p` inside `p`. Jsoup usually splits them.
        // If Jsoup splits them, we might see multiple P elements.

        String case2 = Files.readString(Paths.get("cases/case2.html"));
        String result2 = converter.convert(case2);
        System.out.println("--- Case 2 Result ---");
        System.out.println(result2);
        // We expect "## 一、基础概念..."
        assertTrue(result2.contains("## **一、基础概念")); // inner text is likely bolded by strong

        // Test Case 3
        String case3 = Files.readString(Paths.get("cases/case3.html"));
        String result3 = converter.convert(case3);
        System.out.println("--- Case 3 Result ---");
        System.out.println(result3);
        // The text is "当 AI 智能体..."
        assertTrue(result3.contains("# **当 AI 智能体"));
    }
}
