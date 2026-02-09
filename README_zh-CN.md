# HTML 转 Markdown 转换器

[English](README.md)

[![](https://jitpack.io/v/com.jingwei/html-to-markdown.svg)](https://jitpack.io/#com.jingwei/html-to-markdown)

一个轻量级、高性能且可扩展的 Java 库，用于将 HTML 内容转换为 Markdown 格式。基于 [Jsoup](https://jsoup.org/) 构建，利用现代 Java 特性提供了简洁的 API 和强大的自定义能力。

## 特性 (Features)

- **高性能**: 无需预编译正则，使用 O(1) 的处理器查找，适合高并发或大文档的处理。
- **健壮性**: 完美处理复杂的嵌套结构、列表、表格和混合内容，避免格式混乱。
- **输出整洁**: 处理器直接生成格式良好的 Markdown，极大减少了后期正则清理的开销。
- **零依赖**: 除了 Jsoup 之外没有任何第三方依赖。
- **可扩展**: 支持使用 Lambda 表达式轻松添加自定义处理器。
- **可配置**: 灵活的配置选项，可以选择保留特定的 HTML 标签或完全移除它们。
- **Java 17+**: 专为现代 Java 应用构建，利用函数式接口和 records 特性。

## 安装 (Installation)

本项目托管在 [JitPack](https://jitpack.io) 上。

### Maven

1. 在构建文件中添加 JitPack 仓库：

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

2. 添加依赖：

```xml
<dependency>
    <groupId>com.github.jingwei</groupId>
    <artifactId>html-to-markdown</artifactId>
    <version>v1.0-beta.2</version>
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

## 使用方法 (Usage)

### 基础用法

最简单的使用方式是使用默认配置：

```java
import com.github.htmltomd.HtmlToMarkdownConverter;

public class Example {
    public static void main(String[] args) {
        // 创建默认配置的转换器
        HtmlToMarkdownConverter converter = new HtmlToMarkdownConverter();
        
        String html = "<h1>你好世界</h1><p>这是一段 <strong>粗体</strong> 文字。</p>";
        String markdown = converter.convert(html);
        
        System.out.println(markdown);
    }
}
```

**输出:**
```markdown
# 你好世界

这是一段 **粗体** 文字。
```

### 配置 (保留/移除标签)

你可以使用 `ConverterConfig` 自定义转换过程：

```java
import com.github.htmltomd.HtmlToMarkdownConverter;
import com.github.htmltomd.ConverterConfig;

public class ConfigExample {
    public static void main(String[] args) {
        ConverterConfig config = ConverterConfig.builder()
            .preserveTag("sup")      // 保留 <sup> 标签为原始 HTML
            .preserveTag("sub")      // 保留 <sub> 标签为原始 HTML
            .removeTag("script")     // 完全移除 <script> 标签及其内容
            .removeTag("style")      // 完全移除 <style> 标签及其内容
            .build();

        HtmlToMarkdownConverter converter = new HtmlToMarkdownConverter(config);
        
        String html = "E = mc<sup>2</sup> <script>alert('bad')</script>";
        System.out.println(converter.convert(html));
    }
}
```

**输出:**
```markdown
E = mc<sup>2</sup>
```

### 自定义元素处理器 (Lambda 支持)

你可以使用简单的 Lambda 表达式为特定标签定义自己的转换逻辑。这比传统的接口实现更加简洁。

```java
import com.github.htmltomd.HtmlToMarkdownConverter;
import com.github.htmltomd.ConverterConfig;

public class CustomHandlerExample {
    public static void main(String[] args) {
        // 使用 Lambda 表达式处理 <mark> 标签
        ConverterConfig config = ConverterConfig.builder()
            .addCustomHandler("mark", (element, context) -> {
                String content = context.processChildren(element);
                return "==" + content + "=="; // 自定义高亮语法
            })
            // 你也可以覆盖默认处理器，例如将 <h1> 转换为全大写
            .addCustomHandler("h1", (element, context) -> {
                String content = context.processChildren(element).toUpperCase();
                return "\n# " + content + "\n\n";
            })
            .build();

        HtmlToMarkdownConverter converter = new HtmlToMarkdownConverter(config);
        System.out.println(converter.convert("<h1>标题</h1>带 <mark>高亮</mark> 的文本"));
    }
}
```

**输出:**
```markdown
# 标题

带 ==高亮== 的文本
```

## 支持的元素

默认转换器支持标准的 Markdown 语法映射：

| HTML 元素              | Markdown 等效  | 说明                           |
| ---------------------- | -------------- | ------------------------------ |
| `<h1>`-`<h6>`          | `#` - `######` | 确保标题前后有正确换行         |
| `<p>`                  | 段落           | 使用双换行符分隔               |
| `<br>`                 | 换行           | 两个空格 + 换行符              |
| `<strong>`, `<b>`      | `**粗体**`     | 自动去除首尾空格，处理重复嵌套 |
| `<em>`, `<i>`          | `*斜体*`       |                                |
| `<s>`, `<del>`         | `~~删除线~~`   |                                |
| `<ul>`, `<ol>`, `<li>` | 列表           | 支持无限嵌套和混合列表类型     |
| `<a>`                  | `[标题](链接)` |                                |
| `<img>`                | `![Alt](链接)` |                                |
| `<blockquote>`         | `> 引用`       |                                |
| `<pre>`, `<code>`      | 代码块         | 支持自动语言检测               |
| `<table>`              | 表格           | 标准 Markdown 表格             |
| `<hr>`                 | `---`          |                                |

## 源码构建

要在本地构建项目，你需要 JDK 17+ 和 Maven。

```bash
git clone https://github.com/jingwei/html-to-markdown.git
cd html-to-markdown
mvn clean install
```

## 许可证 (License)

本项目基于 MIT 许可证开源。
