package com.github.htmltomd;

import com.github.htmltomd.handler.ElementHandler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Configuration for HTML to Markdown conversion.
 */
public class ConverterConfig {

    private final Map<String, ElementHandler> customHandlers;
    private final Set<String> preserveTags;
    private final Set<String> removeTags;

    private ConverterConfig(Builder builder) {
        this.customHandlers = new HashMap<>(builder.customHandlers);
        this.preserveTags = new HashSet<>(builder.preserveTags);
        this.removeTags = new HashSet<>(builder.removeTags);
    }

    /**
     * Gets custom handler for a tag.
     * 
     * @param tag the tag name
     * @return the custom handler, or null if none
     */
    public ElementHandler getCustomHandler(String tag) {
        return customHandlers.get(tag.toLowerCase());
    }

    /**
     * Gets all custom handlers.
     * 
     * @return map of all custom handlers
     */
    public Map<String, ElementHandler> getCustomHandlers() {
        return new HashMap<>(customHandlers);
    }

    /**
     * Checks if a tag should be preserved as HTML.
     * 
     * @param tag the tag name
     * @return true if should preserve
     */
    public boolean shouldPreserveTag(String tag) {
        return preserveTags.contains(tag.toLowerCase());
    }

    /**
     * Checks if a tag should be removed completely.
     * 
     * @param tag the tag name
     * @return true if should remove
     */
    public boolean shouldRemoveTag(String tag) {
        return removeTags.contains(tag.toLowerCase());
    }

    /**
     * Creates a new default configuration.
     * 
     * @return default configuration
     */
    public static ConverterConfig defaultConfig() {
        return builder().build();
    }

    /**
     * Creates a new builder.
     * 
     * @return builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for ConverterConfig.
     */
    public static class Builder {
        private final Map<String, ElementHandler> customHandlers = new HashMap<>();
        private final Set<String> preserveTags = new HashSet<>();
        private final Set<String> removeTags = new HashSet<>();

        /**
         * Adds a custom handler for a specific tag.
         * 
         * @param tag     the tag name
         * @param handler the handler
         * @return this builder
         */
        public Builder addCustomHandler(String tag, ElementHandler handler) {
            customHandlers.put(tag.toLowerCase(), handler);
            return this;
        }

        /**
         * Adds a tag to preserve as HTML.
         * 
         * @param tag the tag name
         * @return this builder
         */
        public Builder preserveTag(String tag) {
            preserveTags.add(tag.toLowerCase());
            return this;
        }

        /**
         * Adds a tag to remove completely.
         * 
         * @param tag the tag name
         * @return this builder
         */
        public Builder removeTag(String tag) {
            removeTags.add(tag.toLowerCase());
            return this;
        }

        /**
         * Builds the configuration.
         * 
         * @return the built configuration
         */
        public ConverterConfig build() {
            return new ConverterConfig(this);
        }
    }
}
