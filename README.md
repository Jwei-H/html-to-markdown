# HTML to Markdown Converter

一个轻量级、可扩展的 Java 工具库，用于将 HTML 转换为 Markdown 格式。

## 特性

- ✅ **简洁高效** - 仅依赖 Jsoup，代码简洁易懂
- ✅ **可扩展** - 支持自定义标签转换规则
- ✅ **健壮性** - 处理嵌套结构、异常情况和边界条件
- ✅ **完整测试** - 39 个单元测试和集成测试，覆盖所有支持的语法

## 支持的 Markdown 语法

### 块级元素
- 标题 (h1-h6)
- 段落 (p)
- 列表 (ul, ol, li) - 支持嵌套
- 引用块 (blockquote)
- 代码块 (pre, code) - 支持语言标记
- 表格 (table, thead, tbody, tr, th, td)
- 分隔线 (hr)

### 行内元素
- 粗体 (strong, b)
- 斜体 (em, i)
- 删除线 (del, s)
-行内代码 (code)
- 链接 (a) - 支持标题属性
- 图片 (img) - 支持 alt 和 title
- 换行 (br)

## 快速开始

### Maven 依赖

```xml
<dependency>
    <groupId>com.jingwei</groupId>
    <artifactId>html-to-markdown</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### 基本使用

```java
import com.github.htmltomd.HtmlToMarkdownConverter;

public class Example {
    public static void main(String[] args) {
        HtmlToMarkdownConverter converter = new HtmlToMarkdownConverter();
        
        String html = "<h1>Hello World</h1><p>This is a <strong>test</strong>.</p>";
        String markdown = converter.convert(html);
        
        System.out.println(markdown);
        // 输出:
        // # Hello World
        // 
        // This is a **test**.
    }
}
```

### 自定义配置

```java
import com.github.htmltomd.ConverterConfig;
import com.github.htmltomd.HtmlToMarkdownConverter;

public class CustomExample {
    public static void main(String[] args) {
        HtmlToMarkdownConverter converter = new HtmlToMarkdownConverter();
        
        // 创建自定义配置
        ConverterConfig config = ConverterConfig.builder()
            .preserveTag("sup")      // 保留 <sup> 标签
            .preserveTag("sub")      // 保留 <sub> 标签
            .removeTag("script")     // 完全移除 <script> 标签
            .removeTag("style")      // 完全移除 <style> 标签
            .build();
        
        String html = "<p>E=mc<sup>2</sup></p><script>alert('test');</script>";
        String markdown = converter.convert(html, config);
        
        System.out.println(markdown);
        // 输出: E=mc<sup>2</sup>
        // (script 标签被移除)
    }
}
```

### 自定义标签处理器

```java
import com.github.htmltomd.ConverterConfig;
import com.github.htmltomd.HtmlToMarkdownConverter;
import com.github.htmltomd.handler.ElementHandler;
import com.github.htmltomd.handler.HandlerContext;
import org.jsoup.nodes.Element;

public class CustomHandlerExample {
    public static void main(String[] args) {
        // 创建自定义处理器
        ElementHandler customHandler = new ElementHandler() {
            @Override
            public boolean canHandle(Element element) {
                return "mark".equals(element.tagName());
            }
            
            @Override
            public String handle(Element element, HandlerContext context) {
                // 将 <mark> 转换为 ==text==
                String text = context.processChildren(element);
                return "==" + text + "==";
            }
        };
        
        ConverterConfig config = ConverterConfig.builder()
            .addCustomHandler("mark", customHandler)
            .build();
        
        HtmlToMarkdownConverter converter = new HtmlToMarkdownConverter();
        String html = "<p>This is <mark>highlighted</mark> text.</p>";
        String markdown = converter.convert(html, config);
        
        System.out.println(markdown);
        // 输出: This is ==highlighted== text.
    }
}
```

## 项目结构

```
src/main/java/com/github/htmltomd/
├── HtmlToMarkdownConverter.java    # 主转换器
├── ConverterConfig.java             # 配置类
├── handler/
│   ├── ElementHandler.java          # 处理器接口
│   ├── HandlerContext.java          # 转换上下文
│   └── impl/                        # 内置处理器实现
│       ├── HeadingHandler.java
│       ├── ParagraphHandler.java
│       ├── LinkHandler.java
│       ├── ImageHandler.java
│       ├── EmphasisHandler.java
│       ├── CodeHandler.java
│       ├── ListHandler.java
│       ├── BlockquoteHandler.java
│       ├── TableHandler.java
│       ├── HorizontalRuleHandler.java
│       └── LineBreakHandler.java
└── util/
    └── MarkdownUtils.java           # 工具类
```

## 构建和测试

```bash
# 编译
mvn compile

# 运行测试
mvn test

# 打包
mvn package
```

## 扩展点

### 1. 自定义标签处理器

实现 `ElementHandler` 接口创建自定义处理器：

```java
public interface ElementHandler {
    boolean canHandle(Element element);
    String handle(Element element, HandlerContext context);
}
```

### 2. 配置选项

- `addCustomHandler(tag, handler)` - 添加自定义处理器
- `preserveTag(tag)` - 保留特定标签为 HTML
- `removeTag(tag)` - 完全移除特定标签

## 设计原则

1. **简洁** - 核心代码精简，易于理解和维护
2. **可扩展** - 通过处理器模式支持自定义转换规则
3. **健壮** - 处理各种边界情况和嵌套结构
4. **高效** - 最小化依赖，仅使用 Jsoup 进行 HTML 解析

## 依赖

- Java 17+
- Jsoup 1.17.2 (HTML 解析)

## 许可证

MIT License
