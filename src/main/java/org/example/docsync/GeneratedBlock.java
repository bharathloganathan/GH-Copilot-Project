package org.example.docsync;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class GeneratedBlock {
    private GeneratedBlock() {
    }

    static String replace(String document, String id, String content) throws IOException {
        Pattern begins = Pattern.compile("(?m)^<!-- docsync:begin " + Pattern.quote(id)
                + "(?:[\\t ]+[^\\r\\n>]*)? -->[\\t ]*(?=\\r?$)");
        Pattern ends = Pattern.compile("(?m)^<!-- docsync:end " + Pattern.quote(id)
                + " -->[\\t ]*(?=\\r?$)");
        Matcher begin = begins.matcher(document);
        Matcher end = ends.matcher(document);
        if (!begin.find()) {
            throw new IOException("Missing or malformed begin marker: " + id);
        }
        int start = begin.end();
        if (begin.find() || !end.find()) {
            throw new IOException("Duplicate or missing marker: " + id);
        }
        int finish = end.start();
        if (end.find() || finish < start) {
            throw new IOException("Duplicate or misplaced end marker: " + id);
        }
        String old = document.substring(start, finish);
        if (old.contains("<!-- docsync:begin") || old.contains("<!-- docsync:end")) {
            throw new IOException("Nested or mismatched marker: " + id);
        }
        if (content.contains("<!-- docsync:")) {
            throw new IOException("Generated content contains a documentation marker");
        }
        String newline = document.contains("\r\n") ? "\r\n" : "\n";
        String body = content.replace("\r\n", "\n").replace("\r", "\n").strip();
        return document.substring(0, start) + newline + body.replace("\n", newline) + newline
                + document.substring(finish);
    }
}
