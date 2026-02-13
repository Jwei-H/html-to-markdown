package com.github.htmltomd.handler.impl;

import com.github.htmltomd.ConverterConfig;
import com.github.htmltomd.handler.ElementHandler;
import com.github.htmltomd.handler.HandlerContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for individual element handlers.
 */
class ElementHandlersTest {

    private HandlerContext context;

    @BeforeEach
    void setUp() {
        // Create handler map for testing
        Map<String, ElementHandler> handlers = new HashMap<>();
        handlers.put("h1", new HeadingHandler());
        handlers.put("h2", new HeadingHandler());
        handlers.put("h3", new HeadingHandler());
        handlers.put("p", new ParagraphHandler());
        handlers.put("a", new LinkHandler());
        handlers.put("img", new ImageHandler());
        handlers.put("strong", new EmphasisHandler());
        handlers.put("em", new EmphasisHandler());
        handlers.put("code", new CodeHandler());
        handlers.put("pre", new CodeHandler());
        handlers.put("ul", new ListHandler());
        handlers.put("ol", new ListHandler());
        handlers.put("blockquote", new BlockquoteHandler());
        handlers.put("table", new TableHandler());
        handlers.put("hr", new HorizontalRuleHandler());
        handlers.put("br", new LineBreakHandler());
        context = new HandlerContext(ConverterConfig.defaultConfig(), handlers);
    }

    @Test
    void testHeadingHandler() {
        HeadingHandler handler = new HeadingHandler();

        Element h1 = Jsoup.parse("<h1>Title</h1>").body().child(0);
        assertEquals("\n# Title\n\n", handler.handle(h1, context));

        Element h3 = Jsoup.parse("<h3>Subtitle</h3>").body().child(0);
        assertEquals("\n### Subtitle\n\n", handler.handle(h3, context));
    }

    @Test
    void testHeadingHandlerOptimizations() {
        HeadingHandler handler = new HeadingHandler();

        // Empty heading
        Element empty = Jsoup.parse("<h1></h1>").body().child(0);
        assertEquals("", handler.handle(empty, context));

        // Whitespace only
        Element whitespace = Jsoup.parse("<h2>   </h2>").body().child(0);
        assertEquals("", handler.handle(whitespace, context));

        // Image only
        Element imageOnly = Jsoup.parse("<h1><img src='img.png' alt='alt'></h1>").body().child(0);
        assertEquals("![alt](img.png)\n\n", handler.handle(imageOnly, context));

        // Image with text
        Element imageText = Jsoup.parse("<h1>Text <img src='img.png' alt='alt'></h1>").body().child(0);
        String result = handler.handle(imageText, context);
        assertTrue(result.startsWith("\n# Text"));
        assertTrue(result.contains("![alt](img.png)"));
    }

    @Test
    void testParagraphHandler() {
        ParagraphHandler handler = new ParagraphHandler();

        Element p = Jsoup.parse("<p>Text</p>").body().child(0);
        assertEquals("Text\n\n", handler.handle(p, context));

        Element empty = Jsoup.parse("<p></p>").body().child(0);
        assertEquals("", handler.handle(empty, context));
    }

    @Test
    void testLinkHandler() {
        LinkHandler handler = new LinkHandler();

        Element linkNoTitle = Jsoup.parse("<a href=\"http://example.com\">Link</a>").body().child(0);
        assertEquals("[Link](http://example.com)", handler.handle(linkNoTitle, context));

        Element linkWithTitle = Jsoup.parse("<a href=\"http://example.com\" title=\"Title\">Link</a>").body().child(0);
        assertEquals("[Link](http://example.com \"Title\")", handler.handle(linkWithTitle, context));
    }

    @Test
    void testImageHandler() {
        ImageHandler handler = new ImageHandler();

        Element img = Jsoup.parse("<img src=\"image.jpg\" alt=\"Alt\">").body().child(0);
        assertEquals("![Alt](image.jpg)", handler.handle(img, context));

        Element imgWithTitle = Jsoup.parse("<img src=\"image.jpg\" alt=\"Alt\" title=\"Title\">").body().child(0);
        assertEquals("![Alt](image.jpg \"Title\")", handler.handle(imgWithTitle, context));
    }

    @Test
    void testEmphasisHandler() {
        EmphasisHandler handler = new EmphasisHandler();

        Element strong = Jsoup.parse("<strong>Bold</strong>").body().child(0);
        assertEquals("**Bold**", handler.handle(strong, context));

        Element em = Jsoup.parse("<em>Italic</em>").body().child(0);
        assertEquals("*Italic*", handler.handle(em, context));

        Element del = Jsoup.parse("<del>Strike</del>").body().child(0);
        assertEquals("~~Strike~~", handler.handle(del, context));
    }

    @Test
    void testCodeHandler() {
        CodeHandler handler = new CodeHandler();

        Element code = Jsoup.parse("<code>code</code>").body().child(0);
        assertEquals("`code`", handler.handle(code, context));

        Element pre = Jsoup.parse("<pre><code>code block</code></pre>").body().child(0);
        assertEquals("```\ncode block\n```\n\n", handler.handle(pre, context));

        Element preWithLang = Jsoup.parse("<pre><code class=\"language-java\">code</code></pre>").body().child(0);
        String result = handler.handle(preWithLang, context);
        assertTrue(result.startsWith("```java"));
    }

    @Test
    void testCodeHandlerWithLangAttribute() {
        CodeHandler handler = new CodeHandler();

        // Test lang attribute (should take priority)
        Element codeWithLang = Jsoup.parse("<pre><code class=\"hljs language-json\" lang=\"json\">code</code></pre>")
                .body().child(0);
        String result = handler.handle(codeWithLang, context);
        assertTrue(result.startsWith("```json"));

        // Test multiple classes
        Element codeWithMultiClass = Jsoup.parse("<pre><code class=\"hljs language-python\">code</code></pre>").body()
                .child(0);
        result = handler.handle(codeWithMultiClass, context);
        assertTrue(result.startsWith("```python"));
    }

    @Test
    void testBlockquoteHandler() {
        BlockquoteHandler handler = new BlockquoteHandler();

        Element blockquote = Jsoup.parse("<blockquote>Quote</blockquote>").body().child(0);
        String result = handler.handle(blockquote, context);
        assertTrue(result.contains("> Quote"));
    }

    @Test
    void testHorizontalRuleHandler() {
        HorizontalRuleHandler handler = new HorizontalRuleHandler();

        Element hr = Jsoup.parse("<hr>").body().child(0);
        assertEquals("---\n\n", handler.handle(hr, context));
    }

    @Test
    void testLineBreakHandler() {
        LineBreakHandler handler = new LineBreakHandler();

        Element br = Jsoup.parse("<br>").body().child(0);
        assertEquals("  \n", handler.handle(br, context));
    }
}
