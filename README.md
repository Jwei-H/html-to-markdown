# HTML to Markdown Converter

[![](https://jitpack.io/v/com.jingwei/html-to-markdown.svg)](https://jitpack.io/#com.jingwei/html-to-markdown)

A lightweight, extensible, and robust Java library for converting HTML content into Markdown format. Built on top of [Jsoup](https://jsoup.org/), it offers a clean API with powerful customization options.

## Features

- **Robust Conversion**: Handles standard HTML elements including tables, lists (nested), images, links, and text formatting.
- **Code Block Protection**: Smartly handles fenced code blocks (```) and indented code blocks during conversion to prevent corruption.
- **Clean Output**: Automatically cleans up excessive newlines, fixes list spacing, and normalizes headings and emphasis.
- **Extensible**: Easily add custom handlers for specific HTML tags.
- **Configurable**: Choose to preserve specific HTML tags or remove them entirely from the output.
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
    <version>1.0-SNAPSHOT</version>
</dependency>
```

## Usage

### Basic Usage

The simplest way to use the converter is with the default configuration:

```java
import com.github.htmltomd.HtmlToMarkdownConverter;

public class Example {
    public static void main(String[] args) {
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

### Custom Element Handlers

You can define your own transformation logic for specific tags by implementing `ElementHandler`:

```java
import com.github.htmltomd.HtmlToMarkdownConverter;
import com.github.htmltomd.ConverterConfig;
import com.github.htmltomd.handler.ElementHandler;
import com.github.htmltomd.handler.HandlerContext;
import org.jsoup.nodes.Element;

public class CustomHandlerExample {
    public static void main(String[] args) {
        ElementHandler markHandler = new ElementHandler() {
            @Override
            public boolean canHandle(Element element) {
                return "mark".equals(element.tagName());
            }

            @Override
            public String handle(Element element, HandlerContext context) {
                // Determine how to handle children
                String content = context.processChildren(element);
                return "==" + content + "=="; // Custom markdown syntax for highlighting
            }
        };

        ConverterConfig config = ConverterConfig.builder()
            .addCustomHandler("mark", markHandler)
            .build();

        HtmlToMarkdownConverter converter = new HtmlToMarkdownConverter(config);
        System.out.println(converter.convert("Text with <mark>highlight</mark>"));
    }
}
```

## Supported Elements

The default converter supports standard Markdown syntax mappings:

| HTML Element | Markdown Equivalent |
|--------------|---------------------|
| `<h1>`-`<h6>` | `#` - `######` |
| `<p>` | Paragraph (double newline) |
| `<br>` | Line break |
| `<strong>`, `<b>` | `**Bold**` |
| `<em>`, `<i>` | `*Italic*` |
| `<ul>`, `<ol>`, `<li>` | Lists (Ordered & Unordered), Supports nesting |
| `<a>` | `[Link Title](URL)` |
| `<img>` | `![Alt Text](URL "Title")` |
| `<blockquote>` | `> Quote` |
| `<pre>`, `<code>` | Code blocks and inline code |
| `<table>`, `<tr>`, `<td>`, etc. | Markdown Tables |
| `<hr>` | `---` |

## Building from Source

To build the project locally, you need JDK 17+ and Maven.

```bash
git clone https://github.com/yourusername/html-to-markdown.git
cd html-to-markdown
mvn clean install
```

## License

This project is licensed under the MIT License.