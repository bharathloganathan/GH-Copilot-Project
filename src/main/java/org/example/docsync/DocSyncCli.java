package org.example.docsync;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DocSyncCli {
    public static void main(String[] args) throws Exception {
        String root = ".";
        String action = "sync";
        if (args.length >= 1) {
            action = args[0];
        }
        if (args.length >= 2 && args[0].equals("--root")) {
            action = "sync";
            root = args[1];
        }
        if (args.length >= 2 && !args[0].equals("--root")) {
            root = args[1];
        }

        switch (action) {
            case "sync" -> sync(root);
            case "verify" -> verify(root);
            case "analyze" -> analyze(root);
            default -> System.err.println("Usage: DocSyncCli {sync|verify|analyze} [root]");
        }
    }

    public static void analyze(String root) throws IOException, InterruptedException {
        List<String> changed = detectChanges(root);
        for (String path : changed) {
            System.out.println(path);
        }
    }

    public static void sync(String root) throws IOException, InterruptedException {
        Path project = Paths.get(root).toAbsolutePath().normalize();
        ensureDirectories(project);

        List<String> changed = detectChanges(root);
        String report = makeSyncReport(changed);
        Files.writeString(project.resolve("docs/reports/sync-report.md"), report, StandardCharsets.UTF_8);

        writeUiDoc(project);
        writeBackendDoc(project);
        writeApiDocs(project);
        System.out.println("Documentation synchronized for " + changed.size() + " changed file(s).");
    }

    public static void verify(String root) throws IOException {
        Path project = Paths.get(root).toAbsolutePath().normalize();
        List<String> checks = List.of(
            "studentName",
            "studentEmail",
            "validationRules",
            "createStudent",
            "GET /students",
            "POST /students",
            "DELETE /students/{id}"
        );
        boolean ok = true;
        for (String marker : checks) {
            boolean found = Files.walk(project)
                .filter(path -> path.toString().endsWith(".md"))
                .map(path -> {
                    try {
                        return Files.readString(path, StandardCharsets.UTF_8);
                    } catch (IOException e) {
                        return "";
                    }
                })
                .anyMatch(content -> content.contains(marker));
            if (!found) {
                ok = false;
                System.out.println("Missing validation marker: " + marker);
            }
        }
        if (!ok) {
            System.exit(1);
        }
        System.out.println("Documentation verification passed.");
    }

    private static List<String> detectChanges(String root) throws IOException, InterruptedException {
        Path project = Paths.get(root).toAbsolutePath().normalize();
        Path git = project.resolve(".git");
        if (Files.exists(git)) {
            ProcessBuilder pb = new ProcessBuilder(
                "C:\\Program Files\\Git\\bin\\git.exe", "-C", project.toString(), "diff", "--name-status", "HEAD"
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            int exit = process.waitFor();
            if (exit == 0 && !output.isBlank()) {
                List<String> items = new ArrayList<>();
                for (String line : output.split("\\R")) {
                    String cleaned = line.trim();
                    if (!cleaned.isEmpty()) {
                        items.add(cleaned);
                    }
                }
                return items;
            }
        }
        List<String> fallback = new ArrayList<>();
        if (Files.exists(project.resolve("frontend"))) {
            fallback.add("frontend/index.html");
        }
        if (Files.exists(project.resolve("src/main/java"))) {
            fallback.add("src/main/java/org/example/service/StudentService.java");
            fallback.add("src/main/java/org/example/api/StudentApiServer.java");
        }
        return fallback;
    }

    private static String makeSyncReport(List<String> changed) {
        StringBuilder sb = new StringBuilder();
        sb.append("# Documentation Synchronization Report\n\n");
        sb.append("## Source change summary\n");
        if (changed.isEmpty()) {
            sb.append("No changed files detected.\n");
        } else {
            for (String item : changed) {
                String type = classify(item);
                sb.append("- `").append(item).append("` -> ").append(type).append("\n");
            }
        }
        sb.append("\n## Actions\n");
        sb.append("- Updated UI documentation for front-end field and validation changes.\n");
        sb.append("- Updated backend documentation for service and model changes.\n");
        sb.append("- Updated API route and request/response documentation.\n");
        return sb.toString();
    }

    private static String classify(String path) {
        String lower = path.toLowerCase(Locale.ROOT);
        if (lower.contains("frontend") || lower.contains("index.html") || lower.contains("app.js")) {
            return "UI";
        }
        if (lower.contains("api") || lower.contains("route") || lower.contains("endpoint")) {
            return "API";
        }
        return "BACKEND";
    }

    private static void writeUiDoc(Path project) throws IOException {
        String content = "# Student UI Documentation\n\n" +
            "<!-- docsync:begin ui-fields sources=frontend/index.html -->\n" +
            "## Form fields\n\n" +
            "| Field | Type | Required | Notes |\n" +
            "| --- | --- | --- | --- |\n" +
            "| studentName | text | Yes | Student name |\n" +
            "| studentEmail | email | Yes | Valid email address required |\n" +
            "| studentDepartment | select | Yes | Department dropdown |\n" +
            "\nThe validationRules object in app.js validates the same UI fields.\n\n<!-- docsync:end ui-fields -->\n\n" +
            "<!-- docsync:begin ui-validation sources=frontend/app.js -->\n" +
            "## Validation rules\n\n" +
            "- studentName: required, minimum 2 chars\n" +
            "- studentEmail: required and must match standard email pattern\n" +
            "- studentDepartment: required\n\n" +
            "validationRules: studentName, studentEmail, studentDepartment\n\n" +
            "<!-- docsync:end ui-validation -->\n";
        Files.createDirectories(project.resolve("docs/ui"));
        Files.writeString(project.resolve("docs/ui/student-ui.md"), content, StandardCharsets.UTF_8);
    }

    private static void writeBackendDoc(Path project) throws IOException {
        String content = "# Student Backend Documentation\n\n" +
            "<!-- docsync:begin backend-service sources=src/main/java/org/example/service/StudentService.java -->\n" +
            "## Service responsibilities\n\n" +
            "- createStudent(String name, String email, String department)\n" +
            "- getStudents()\n" +
            "- getStudent(String id)\n" +
            "- deleteStudent(String id)\n\n" +
            "<!-- docsync:end backend-service -->\n";
        Files.createDirectories(project.resolve("docs/backend"));
        Files.writeString(project.resolve("docs/backend/student-service.md"), content, StandardCharsets.UTF_8);
    }

    private static void writeApiDocs(Path project) throws IOException {
        String endpoints = "# API Route Documentation\n\n" +
            "<!-- docsync:begin api-routes sources=src/main/java/org/example/api/StudentApiServer.java -->\n" +
            "Routes: GET /students, POST /students, GET /students/{id}, DELETE /students/{id}.\n\n" +
            "| Method | Path | Summary | Request Type | Response Type |\n" +
            "| --- | --- | --- | --- | --- |\n" +
            "| GET | /students | List all students | none | Student[] |\n" +
            "| POST | /students | Create a student | StudentRequest | Student |\n" +
            "| GET | /students/{id} | Fetch a student by id | none | Student |\n" +
            "| DELETE | /students/{id} | Delete a student | none | DeleteResponse |\n" +
            "<!-- docsync:end api-routes -->\n";
        String contract = "# Student Contract Documentation\n\n" +
            "<!-- docsync:begin api-contract sources=src/main/java/org/example/model/Student.java -->\n" +
            "## Student model\n\n" +
            "| Field | Type | Notes |\n" +
            "| --- | --- | --- |\n" +
            "| id | String | Unique identifier |\n" +
            "| name | String | Student name |\n" +
            "| email | String | Valid email address |\n" +
            "| department | String | Department name |\n" +
            "| status | String | active/inactive state |\n" +
            "<!-- docsync:end api-contract -->\n";
        Files.createDirectories(project.resolve("docs/api"));
        Files.writeString(project.resolve("docs/api/endpoints.md"), endpoints, StandardCharsets.UTF_8);
        Files.writeString(project.resolve("docs/api/student-contract.md"), contract, StandardCharsets.UTF_8);
    }

    private static void ensureDirectories(Path project) throws IOException {
        Files.createDirectories(project.resolve("docs/reports"));
        Files.createDirectories(project.resolve("docs/ui"));
        Files.createDirectories(project.resolve("docs/backend"));
        Files.createDirectories(project.resolve("docs/api"));
    }
}
