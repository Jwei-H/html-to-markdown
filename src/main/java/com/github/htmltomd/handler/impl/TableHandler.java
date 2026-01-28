package com.github.htmltomd.handler.impl;

import com.github.htmltomd.handler.ElementHandler;
import com.github.htmltomd.handler.HandlerContext;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles conversion of table elements to Markdown.
 * Supports: table, thead, tbody, tr, th, td.
 */
public class TableHandler implements ElementHandler {

    @Override
    public boolean canHandle(Element element) {
        return "table".equals(element.tagName().toLowerCase());
    }

    @Override
    public String handle(Element element, HandlerContext context) {
        List<List<String>> rows = new ArrayList<>();
        boolean hasHeader = false;

        // Process thead
        Elements theadRows = element.select("thead > tr");
        if (!theadRows.isEmpty()) {
            hasHeader = true;
            for (Element row : theadRows) {
                rows.add(extractCells(row, context));
            }
        }

        // Process tbody or direct tr
        Elements tbodyRows = element.select("tbody > tr");
        if (tbodyRows.isEmpty()) {
            tbodyRows = element.select("tr");
        }

        for (Element row : tbodyRows) {
            // Skip if already processed in thead
            if (hasHeader && row.parent() != null &&
                    "thead".equals(row.parent().tagName().toLowerCase())) {
                continue;
            }
            List<String> cells = extractCells(row, context);
            // Skip completely empty rows (all cells are empty)
            if (!isRowEmpty(cells)) {
                rows.add(cells);
            }
        }

        if (rows.isEmpty()) {
            return "";
        }

        // If no header, treat first row as header
        if (!hasHeader && !rows.isEmpty()) {
            hasHeader = true;
        }

        return formatTable(rows, hasHeader);
    }

    private List<String> extractCells(Element row, HandlerContext context) {
        List<String> cells = new ArrayList<>();
        Elements cellElements = row.select("th, td");

        for (Element cell : cellElements) {
            String content = context.processChildren(cell).trim();
            // Remove newlines from cell content
            content = content.replace("\n", " ");
            cells.add(content);
        }

        return cells;
    }

    /**
     * Checks if a row is completely empty (all cells have no content).
     */
    private boolean isRowEmpty(List<String> cells) {
        return cells.stream().allMatch(String::isEmpty);
    }

    private String formatTable(List<List<String>> rows, boolean hasHeader) {
        if (rows.isEmpty()) {
            return "";
        }

        // Calculate column widths
        int maxCols = rows.stream().mapToInt(List::size).max().orElse(0);

        StringBuilder result = new StringBuilder();

        // Header row
        List<String> headerRow = rows.get(0);
        result.append("| ");
        for (int i = 0; i < maxCols; i++) {
            String cell = i < headerRow.size() ? headerRow.get(i) : "";
            result.append(cell).append(" | ");
        }
        result.append("\n");

        // Separator row
        result.append("|");
        for (int i = 0; i < maxCols; i++) {
            result.append("-------|");
        }
        result.append("\n");

        // Data rows
        for (int rowIdx = hasHeader ? 1 : 0; rowIdx < rows.size(); rowIdx++) {
            List<String> row = rows.get(rowIdx);
            result.append("| ");
            for (int i = 0; i < maxCols; i++) {
                String cell = i < row.size() ? row.get(i) : "";
                result.append(cell).append(" | ");
            }
            result.append("\n");
        }

        result.append("\n");
        return result.toString();
    }
}
