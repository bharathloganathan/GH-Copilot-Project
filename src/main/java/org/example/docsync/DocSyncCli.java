package org.example.docsync;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class DocSyncCli {
    private DocSyncCli() {
    }

    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                throw new IllegalArgumentException("Usage: DocSyncCli {sync|verify} [--root PATH]");
            }
            String root = ".";
            if (args.length == 2) {
                root = args[1];
            } else if (args.length == 3 && "--root".equals(args[1])) {
                root = args[2];
            } else if (args.length != 1) {
                throw new IllegalArgumentException("Usage: DocSyncCli {sync|verify} [--root PATH]");
            }
            switch (args[0]) {
                case "sync" -> sync(root);
                case "verify" -> verify(root);
                default -> throw new IllegalArgumentException("Unsupported command: " + args[0]);
            }
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("DocSync failed: " + e.getMessage());
            for (Throwable suppressed : e.getSuppressed()) {
                System.err.println("Additional failure: " + suppressed.getMessage());
            }
            System.exit(1);
        }
    }

    public static void sync(String root) throws IOException {
        Path project = Path.of(root).toAbsolutePath().normalize();
        Path report = project.resolve("docs/reports/sync-report.md");
        try {
            UiSync.Prepared prepared = UiSync.prepare(project);
            List<BackendApiSync.Prepared> javaDocs = BackendApiSync.prepare(project);
            List<String> actions = new ArrayList<>();
            actions.add("- `docs/ui/student-ui.md`: " + (prepared.changed() ? "updated" : "already current"));
            for (BackendApiSync.Prepared doc : javaDocs) {
                actions.add("- `" + relative(project, doc.path()) + "`: "
                        + (doc.changed() ? "updated" : "already current"));
                if (doc.changed() && doc.path().endsWith("endpoints.md")) {
                    for (String line : doc.original().lines().filter(line -> line.startsWith("| ")).toList()) {
                        if (!doc.updated().lines().anyMatch(line::equals)) {
                            actions.add("  - Removed/changed declaration row: " + line.replace("`", "\\`"));
                        }
                    }
                }
            }
            UiSync.write(prepared);
            for (BackendApiSync.Prepared doc : javaDocs) {
                BackendApiSync.write(doc);
            }
            verify(root);
            writeReport(report, "# Documentation Synchronization Report\n\n"
                    + "## Status: Synchronized within supported scope\n\n"
                    + "UI blocks, StudentService public methods and digest, declared routes, "
                    + "Student record fields, and literal create-request form parameters were compared "
                    + "against source. Manual text was preserved.\n\n"
                    + String.join("\n", actions) + "\n\n"
                    + "## Limitations and approval\n\n"
                    + "Explicit source scan, not Git impact analysis. Only the four mapped documents "
                    + "are supported; unchanged documents are not rewritten. Route declarations are "
                    + "separate from HTTP dispatch. Model fields are not proof of wire-format behavior. "
                    + "Business-logic meaning requires human review. No commit or merge was performed.\n");
            System.out.println("UI, backend and API documentation synchronized within supported scope.");
        } catch (IOException | IllegalArgumentException e) {
            try {
                writeReport(report, "# Documentation Synchronization Report\n\n"
                        + "## Status: Failed\n\n"
                        + "Reason: " + String.valueOf(e.getMessage()).replace("\r", " ")
                                .replace("\n", " ") + "\n\n"
                        + "Synchronization did not complete. Some documents may have been written; "
                        + "inspect the documentation before retrying.\n");
            } catch (IOException reportError) {
                e.addSuppressed(reportError);
            }
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            throw new IOException("Synchronization failed: " + e.getMessage(), e);
        }
    }

    public static void verify(String root) throws IOException {
        Path project = Path.of(root).toAbsolutePath().normalize();
        Path report = project.resolve("docs/reports/verify-report.md");
        try {
            List<String> stale = new ArrayList<>();
            if (UiSync.prepare(project).changed()) {
                stale.add("docs/ui/student-ui.md");
            }
            for (BackendApiSync.Prepared doc : BackendApiSync.prepare(project)) {
                if (doc.changed()) {
                    stale.add(relative(project, doc.path()));
                }
            }
            if (!stale.isEmpty()) {
                throw new IOException("STALE: " + String.join(", ", stale));
            }
            writeReport(report, "# Verification Report\n\n## Result: PASS within supported scope\n\n"
                    + "The generated UI, StudentService, declared-route, Student-record, and create-request "
                    + "blocks match current source. Manual content and actual HTTP behavior are not verified. "
                    + "Other application sources and documents are outside this verifier's scope.\n");
            System.out.println("Documentation matches source within supported scope.");
        } catch (IOException | IllegalArgumentException e) {
            try {
                writeReport(report, "# Verification Report\n\n## Result: FAIL\n\n"
                        + String.valueOf(e.getMessage()).replace("\r", " ").replace("\n", " ") + "\n");
            } catch (IOException reportFailure) {
                e.addSuppressed(reportFailure);
            }
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            throw new IOException("Invalid documentation or source: " + e.getMessage(), e);
        }
    }

    private static String relative(Path root, Path path) {
        return root.relativize(path).toString().replace('\\', '/');
    }

    private static void writeReport(Path report, String content) throws IOException {
        Files.createDirectories(report.getParent());
        Files.writeString(report, content, StandardCharsets.UTF_8);
    }
}
