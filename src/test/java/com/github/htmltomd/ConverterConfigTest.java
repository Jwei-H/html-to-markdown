package com.github.htmltomd;

import com.github.htmltomd.handler.ElementHandler;
import com.github.htmltomd.handler.HandlerContext;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ConverterConfig.
 */
class ConverterConfigTest {

    @Test
    void testDefaultConfig() {
        ConverterConfig config = ConverterConfig.defaultConfig();
        assertNotNull(config);
        assertNull(config.getCustomHandler("div"));
        assertFalse(config.shouldPreserveTag("div"));
        assertFalse(config.shouldRemoveTag("div"));
    }

    @Test
    void testBuilder() {
        ConverterConfig config = ConverterConfig.builder()
                .preserveTag("sup")
                .preserveTag("sub")
                .removeTag("script")
                .removeTag("style")
                .build();

        assertTrue(config.shouldPreserveTag("sup"));
        assertTrue(config.shouldPreserveTag("SUP")); // case insensitive
        assertTrue(config.shouldRemoveTag("script"));
        assertTrue(config.shouldRemoveTag("SCRIPT")); // case insensitive
    }

    @Test
    void testCustomHandler() {
        ElementHandler customHandler = new ElementHandler() {
            @Override
            public boolean canHandle(Element element) {
                return "custom".equals(element.tagName());
            }

            @Override
            public String handle(Element element, HandlerContext context) {
                return "CUSTOM";
            }
        };

        ConverterConfig config = ConverterConfig.builder()
                .addCustomHandler("custom", customHandler)
                .build();

        assertNotNull(config.getCustomHandler("custom"));
        assertNotNull(config.getCustomHandler("CUSTOM")); // case insensitive
        assertSame(customHandler, config.getCustomHandler("custom"));
    }
}
