package com.github.htmltomd;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NewlineIssueTest {

        @Test
        public void testSpanWithBlockAndText() {
                String html = "<section>" +
                                "<span>" +
                                "<ul><li>Item 1</li></ul>" +
                                "</span>" +
                                "<section>Text after list</section>" +
                                "</section>";

                HtmlToMarkdownConverter converter = new HtmlToMarkdownConverter();
                String markdown = converter.convert(html);

                // Expect newline between list and text
                assertTrue(markdown.contains("- Item 1\n\nText after list")
                                || markdown.contains("- Item 1\nText after list"),
                                "Should have newline between list and text");
        }

        @Test
        public void testHeadingFollowingTextInSpan() {
                String html = "<section>" +
                                "<span>1</span>" +
                                "<span><h4>Title</h4></span>" +
                                "</section>";

                HtmlToMarkdownConverter converter = new HtmlToMarkdownConverter();
                String markdown = converter.convert(html);

                // Expect newline before ####
                assertTrue(markdown.contains("1\n#### Title") || markdown.contains("1\n\n#### Title"),
                                "Should have newline before heading");
        }

        @Test
        public void testReportedIssues() {
                // Case 1: "本文首发于" missing newline
                String html1 = "<section>" +
                                "<section>" +
                                "<span><span><span>全文共</span></span><span><strong><span>4663</span></strong></span><span><span>字，阅读大约需要</span></span><span><strong><span>11</span></strong></span><span><span>分钟</span></span>"
                                +
                                "<ul class=\"list-paddingleft-1\"><li><section><span>List Item</span></section></li></ul>"
                                +
                                "</span>" +
                                "</section>" +
                                "<section><span><strong><strong><span><strong><strong><span>本文</span><strong><span>首发于</span></strong><strong><span>南方周末</span></strong></strong></strong></span></strong></strong></span></section>"
                                +
                                "</section>";

                HtmlToMarkdownConverter converter = new HtmlToMarkdownConverter();
                String markdown1 = converter.convert(html1);

                // Case 2: "我发起疯来自己都打" missing newline
                String html2 = "<section><span><span><span>1</span></span></span><span>&nbsp;</span><span>" +
                                "<h4><strong><span>“我发起疯来自己都打”</span></strong></h4>" +
                                "</span></section>" +
                                "<p><span>互联网巨头们，正...</span></p>";

                String markdown2 = converter.convert(html2);

                assertTrue(markdown1.contains("- List Item\n\n**本文**首发于")
                                || markdown1.contains("- List Item\n**本文**首发于"),
                                "Should separate list and following text. Got:\n" + markdown1);

                assertTrue(markdown2.contains("1\n#### **“我发起疯来自己都打”**")
                                || markdown2.contains("1\n\n#### **“我发起疯来自己都打”**"),
                                "Should separate '1' and heading. Got:\n" + markdown2);
        }
}
