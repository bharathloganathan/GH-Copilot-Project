package org.example.docsync;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class UiSync {
    private static final String HTML_SOURCE = "frontend/index.html";
    private static final String JS_SOURCE = "frontend/app.js";
    private static final String DOC = "docs/ui/student-ui.md";
    private static final Pattern VALIDATION = Pattern.compile("\\b(?:const|let|var)\\s+validationRules\\s*=\\s*\\{");

    record Prepared(Path path, String original, String updated) {
        boolean changed() {
            return !original.equals(updated);
        }
    }

    private UiSync() {
    }

    static Prepared prepare(Path root) throws IOException {
        String html = read(root.resolve(HTML_SOURCE));
        String javascript = read(root.resolve(JS_SOURCE));
        Path doc = root.resolve(DOC);
        String original = read(doc);
        String updated = replace(original, "ui-fields", fields(html));
        updated = replace(updated, "ui-validation", validation(javascript));
        return new Prepared(doc, original, updated);
    }

    static void write(Prepared prepared) throws IOException {
        if (!prepared.changed()) {
            return;
        }
        if (!read(prepared.path()).equals(prepared.original())) {
            throw new IOException("UI documentation changed during synchronization");
        }
        Files.writeString(prepared.path(), prepared.updated(), StandardCharsets.UTF_8);
    }

    private static String read(Path path) throws IOException {
        if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException("Missing or unsupported source/document: " + path);
        }
        return Files.readString(path, StandardCharsets.UTF_8);
    }

    private static String fields(String html) throws IOException {
        List<String> rows = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        HTMLEditorKit.ParserCallback callback = new HTMLEditorKit.ParserCallback() {
            private void add(HTML.Tag tag, MutableAttributeSet attributes) {
                if (tag != HTML.Tag.INPUT && tag != HTML.Tag.SELECT && tag != HTML.Tag.TEXTAREA) {
                    return;
                }
                String id = attribute(attributes, "id");
                if (id == null || id.isBlank() || !ids.add(id)) {
                    errors.add("Form control has a missing or duplicate ID: " + id);
                    return;
                }
                String type = tag == HTML.Tag.INPUT ? attribute(attributes, "type") : tag.toString();
                if (type == null || type.isBlank()) {
                    type = "text";
                }
                rows.add("| " + cell(id) + " | " + cell(type) + " | "
                        + (attribute(attributes, "required") != null ? "Yes" : "No") + " |");
            }

            @Override
            public void handleStartTag(HTML.Tag tag, MutableAttributeSet attrs, int position) {
                add(tag, attrs);
            }

            @Override
            public void handleSimpleTag(HTML.Tag tag, MutableAttributeSet attrs, int position) {
                add(tag, attrs);
            }
        };
        new ParserDelegator().parse(new StringReader(html), callback, true);
        if (!errors.isEmpty()) {
            throw new IOException(String.join("; ", errors));
        }
        return "## Form fields\n\nSource: `" + HTML_SOURCE + "`\n\n"
                + "| Field ID | Type | Required |\n| --- | --- | --- |\n"
                + String.join("\n", rows);
    }

    private static String attribute(MutableAttributeSet attrs, String name) {
        Enumeration<?> names = attrs.getAttributeNames();
        while (names.hasMoreElements()) {
            Object key = names.nextElement();
            if (name.equalsIgnoreCase(key.toString())) {
                Object value = attrs.getAttribute(key);
                return value == null ? "" : value.toString();
            }
        }
        return null;
    }

    private static String cell(String value) {
        return value.replace("\\", "\\\\").replace("|", "\\|").replace("\r", " ").replace("\n", " ");
    }

    private static String validation(String javascript) throws IOException {
        Matcher declaration = VALIDATION.matcher(javascript);
        if (!declaration.find()) {
            throw new IOException("Missing validationRules declaration");
        }
        int start = declaration.start();
        int opening = declaration.end() - 1;
        if (declaration.find()) {
            throw new IOException("Duplicate validationRules declarations");
        }
        int depth = 1;
        char quote = 0;
        boolean escaped = false;
        boolean lineComment = false;
        boolean blockComment = false;
        boolean regex = false;
        boolean regexClass = false;
        char previous = '{';
        for (int i = opening + 1; i < javascript.length(); i++) {
            char current = javascript.charAt(i);
            char next = i + 1 < javascript.length() ? javascript.charAt(i + 1) : 0;
            if (lineComment) {
                if (current == '\n' || current == '\r') {
                    lineComment = false;
                }
                continue;
            }
            if (blockComment) {
                if (current == '*' && next == '/') {
                    blockComment = false;
                    i++;
                }
                continue;
            }
            if (quote != 0 || regex) {
                if (escaped) {
                    escaped = false;
                } else if (current == '\\') {
                    escaped = true;
                } else if (regex && current == '[') {
                    regexClass = true;
                } else if (regex && current == ']' && regexClass) {
                    regexClass = false;
                } else if (regex && current == '/' && !regexClass) {
                    regex = false;
                    previous = '/';
                } else if (quote != 0 && current == quote) {
                    quote = 0;
                    previous = '"';
                }
                continue;
            }
            if (current == '/' && next == '/') {
                lineComment = true;
                i++;
            } else if (current == '/' && next == '*') {
                blockComment = true;
                i++;
            } else if (current == '/' && (previous == ':' || previous == '=' || previous == '(' || previous == ',')) {
                regex = true;
            } else if (current == '\'' || current == '"') {
                quote = current;
            } else if (current == '`') {
                throw new IOException("Template literals in validationRules are unsupported");
            } else if (current == '{') {
                depth++;
                previous = current;
            } else if (current == '}') {
                if (--depth == 0) {
                    String rules = javascript.substring(start, i + 1);
                    if (rules.contains("```") || rules.contains("<!-- docsync:")) {
                        throw new IOException("Unsupported marker in validation rules");
                    }
                    return "## Validation rules\n\nSource: `" + JS_SOURCE
                            + "`\n\n```javascript\n" + rules + "\n```";
                }
                previous = current;
            } else if (!Character.isWhitespace(current)) {
                previous = current;
            }
        }
        throw new IOException("Unterminated validationRules object");
    }

    private static String replace(String doc, String id, String content) throws IOException {
        Pattern beginPattern = Pattern.compile("(?m)^<!-- docsync:begin " + Pattern.quote(id)
                + "(?:[\\t ]+[^\\r\\n>]*)? -->[\\t ]*\\r?$");
        Pattern endPattern = Pattern.compile("(?m)^<!-- docsync:end " + Pattern.quote(id) + " -->[\\t ]*\\r?$");
        Matcher begin = beginPattern.matcher(doc);
        Matcher end = endPattern.matcher(doc);
        if (!begin.find()) {
            throw new IOException("Missing or malformed begin marker: " + id);
        }
        int start = begin.end();
        if (start > 0 && doc.charAt(start - 1) == '\r') {
            start--;
        }
        if (begin.find() || !end.find()) {
            throw new IOException("Duplicate or missing marker: " + id);
        }
        int finish = end.start();
        if (end.find() || finish < start) {
            throw new IOException("Duplicate or misplaced end marker: " + id);
        }
        String old = doc.substring(start, finish);
        if (old.contains("<!-- docsync:begin") || old.contains("<!-- docsync:end")) {
            throw new IOException("Nested or mismatched marker: " + id);
        }
        String newline = doc.contains("\r\n") ? "\r\n" : "\n";
        String body = content.replace("\r\n", "\n").replace("\r", "\n").strip();
        if (body.contains("<!-- docsync:")) {
            throw new IOException("Generated content contains a documentation marker");
        }
        return doc.substring(0, start) + newline + body.replace("\n", newline) + newline
                + doc.substring(finish);
    }
}
