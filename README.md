# HTML to Markdown Converter

[中文](README_zh-CN.md)

[![](https://jitpack.io/v/com.jingwei/html-to-markdown.svg)](https://jitpack.io/#com.jingwei/html-to-markdown)

A lightweight, high-performance, and extensible Java library for converting HTML content into Markdown format. Built on top of [Jsoup](https://jsoup.org/), it offers a clean API with powerful customization options using modern Java features.

## Features

- **High Performance**: suitable for processing large documents.
- **Robustness**: Handles complex nested structures, lists, tables, and mixed content seamlessly.
- **Clean Output**: Generates well-formatted Markdown directly from handlers, minimizing post-processing regex overhead.
- **Zero-Dependency**: No external dependencies other than Jsoup.
- **Extensible**: Easily add custom handlers using Lambda expressions.
- **Configurable**: Flexible options to preserve specific HTML tags or remove them entirely.
- **Java 17+**: Built for modern Java applications.

## Installation

This project is hosted on [JitPack](https://jitpack.io).

### Maven

1. Add the JitPack repository to your build file:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

2. Add the dependency:

```xml
<dependency>
    <groupId>com.github.jingwei</groupId>
    <artifactId>html-to-markdown</artifactId>
    <version>v1.0-beta.6</version>
</dependency>
```

### Gradle

```groovy
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}

dependencies {
    implementation 'com.github.jingwei:html-to-markdown:v1.0-beta.2'
}
```

## Usage

### Basic Usage

The simplest way to use the converter is with the default configuration:

```java
import com.github.htmltomd.HtmlToMarkdownConverter;

public class Example {
    public static void main(String[] args) {
        // Create a converter with default settings
        HtmlToMarkdownConverter converter = new HtmlToMarkdownConverter();
        
        String html = "<h1>Hello World</h1><p>This is a <strong>bold</strong> statement.</p>";
        String markdown = converter.convert(html);
        
        System.out.println(markdown);
    }
}
```

**Output:**
```markdown
# Hello World

This is a **bold** statement.
```

### Configuration (Preserve/Remove Tags)

You can customize the conversion process using `ConverterConfig`:

```java
import com.github.htmltomd.HtmlToMarkdownConverter;
import com.github.htmltomd.ConverterConfig;

public class ConfigExample {
    public static void main(String[] args) {
        ConverterConfig config = ConverterConfig.builder()
            .preserveTag("sup")      // Keep <sup> tags as HTML
            .preserveTag("sub")      // Keep <sub> tags as HTML
            .removeTag("script")     // Completely remove <script> tags and their content
            .removeTag("style")      // Completely remove <style> tags and their content
            .build();

        HtmlToMarkdownConverter converter = new HtmlToMarkdownConverter(config);
        
        String html = "E = mc<sup>2</sup> <script>alert('bad')</script>";
        System.out.println(converter.convert(html));
    }
}
```

**Output:**
```markdown
E = mc<sup>2</sup>
```

### Custom Element Handlers (Lambda Support)

You can define your own transformation logic for specific tags using simple Lambda expressions. This is much more concise than the traditional interface implementation.

```java
import com.github.htmltomd.HtmlToMarkdownConverter;
import com.github.htmltomd.ConverterConfig;

public class CustomHandlerExample {
    public static void main(String[] args) {
        // Use a lambda expression to handle <mark> tags
        ConverterConfig config = ConverterConfig.builder()
            .addCustomHandler("mark", (element, context) -> {
                String content = context.processChildren(element);
                return "==" + content + "=="; // Custom markdown syntax for highlighting
            })
            // You can also override default handlers, e.g., make <h1> uppercase
            .addCustomHandler("h1", (element, context) -> {
                String content = context.processChildren(element).toUpperCase();
                return "\n# " + content + "\n\n";
            })
            .build();

        HtmlToMarkdownConverter converter = new HtmlToMarkdownConverter(config);
        System.out.println(converter.convert("<h1>Title</h1>Text with <mark>highlight</mark>"));
    }
}
```

**Output:**
```markdown
# TITLE

Text with ==highlight==
```

## Supported Elements

The default converter supports standard Markdown syntax mappings:

| HTML Element           | Markdown Equivalent | Notes                                      |
| ---------------------- | ------------------- | ------------------------------------------ |
| `<h1>`-`<h6>`          | `#` - `######`      | Ensures proper spacing                     |
| `<p>`                  | Paragraph           | Splits with double newline                 |
| `<br>`                 | Line break          | Two spaces + newline                       |
| `<strong>`, `<b>`      | `**Bold**`          | Automatically trims, deduplicates nesting  |
| `<em>`, `<i>`          | `*Italic*`          |                                            |
| `<s>`, `<del>`         | `~~Strikethrough~~` |                                            |
| `<ul>`, `<ol>`, `<li>` | Lists               | Supports unlimited nesting and mixed types |
| `<a>`                  | `[Title](URL)`      |                                            |
| `<img>`                | `![Alt](URL)`       |                                            |
| `<blockquote>`         | `> Quote`           |                                            |
| `<pre>`, `<code>`      | Code blocks         | Supports language detection                |
| `<table>`              | Markdown Tables     |                                            |
| `<hr>`                 | `---`               |                                            |

## Building from Source

To build the project locally, you need JDK 17+ and Maven.

```bash
git clone https://github.com/jingwei/html-to-markdown.git
cd html-to-markdown
mvn clean install
```

## License

This project is licensed under the MIT License.