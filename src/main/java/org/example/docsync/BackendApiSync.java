package org.example.docsync;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates the backend service, API route and API contract blocks from Java source.
 */
final class BackendApiSync {
    static final String SERVICE_SOURCE = "src/main/java/org/example/service/StudentService.java";
    static final String API_SOURCE = "src/main/java/org/example/api/StudentApiServer.java";
    static final String MODEL_SOURCE = "src/main/java/org/example/model/Student.java";
    static final String SERVICE_DOC = "docs/backend/student-service.md";
    static final String ROUTES_DOC = "docs/api/endpoints.md";
    static final String CONTRACT_DOC = "docs/api/student-contract.md";

    record Prepared(Path path, String original, String updated) {
        boolean changed() {
            return !original.equals(updated);
        }
    }

    private BackendApiSync() {
    }

    static List<Prepared> prepare(Path root) throws IOException {
        String service = read(root.resolve(SERVICE_SOURCE));
        String api = read(root.resolve(API_SOURCE));
        String model = read(root.resolve(MODEL_SOURCE));
        try {
            JavaSourceExtractor.Result serviceResult = JavaSourceExtractor.extract(service);
            JavaSourceExtractor.Result apiResult = JavaSourceExtractor.extract(api);
            JavaSourceExtractor.Result modelResult = JavaSourceExtractor.extract(model);

            List<Prepared> result = new ArrayList<>();
            result.add(update(root, SERVICE_DOC, "backend-service", serviceBlock(serviceResult)));
            result.add(update(root, ROUTES_DOC, "api-routes", routesBlock(apiResult)));
            Prepared contract = update(root, CONTRACT_DOC, "api-contract", modelBlock(modelResult));
            String withRequest = GeneratedBlock.replace(contract.updated(), "api-request", requestBlock(apiResult));
            result.add(new Prepared(contract.path(), contract.original(), withRequest));
            return result;
        } catch (IllegalArgumentException e) {
            throw new IOException("Cannot generate backend/API documentation: " + e.getMessage(), e);
        }
    }

    static void write(Prepared prepared) throws IOException {
        if (!prepared.changed()) {
            return;
        }
        if (!read(prepared.path()).equals(prepared.original())) {
            throw new IOException("Documentation changed during synchronization: " + prepared.path());
        }
        Files.writeString(prepared.path(), prepared.updated(), StandardCharsets.UTF_8);
    }

    private static Prepared update(Path root, String doc, String block, String content) throws IOException {
        Path path = root.resolve(doc);
        String original = read(path);
        return new Prepared(path, original, GeneratedBlock.replace(original, block, content));
    }

    private static String read(Path path) throws IOException {
        if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException("Missing or unsupported source/document: " + path);
        }
        return Files.readString(path, StandardCharsets.UTF_8);
    }

    private static JavaSourceExtractor.JavaType type(JavaSourceExtractor.Result result, String name) {
        return result.types().stream().filter(item -> item.name().equals(name)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Type not found in source: " + name));
    }

    private static String serviceBlock(JavaSourceExtractor.Result result) {
        StringBuilder out = new StringBuilder("## Public methods\n\nSource: `" + SERVICE_SOURCE
                + "`\n\n```java\n");
        for (String method : type(result, "StudentService").methods()) {
            out.append(method.replaceAll("\\s+", " ").trim()).append('\n');
        }
        return out.append("```\n\nSource SHA-256: `").append(result.sourceDigest())
                .append("`\n\nLogic changes alter this digest; review source changes for behavioral meaning.")
                .toString();
    }

    private static String routesBlock(JavaSourceExtractor.Result result) {
        StringBuilder out = new StringBuilder("## Declared routes\n\nSource: `" + API_SOURCE + "`\n\n"
                + "These are declarations in `routes()`, not validation of HTTP dispatch.\n\n"
                + "| Method | Path | Summary | Request Type | Response Type |\n"
                + "| --- | --- | --- | --- | --- |\n");
        for (JavaSourceExtractor.Route route : result.routes()) {
            out.append("| ").append(cell(route.method())).append(" | ").append(cell(route.path()))
                    .append(" | ").append(cell(route.summary())).append(" | ")
                    .append(cell(route.requestType())).append(" | ")
                    .append(cell(route.responseType())).append(" |\n");
        }
        return out.toString();
    }

    private static String modelBlock(JavaSourceExtractor.Result result) {
        JavaSourceExtractor.JavaType student = type(result, "Student");
        StringBuilder out = new StringBuilder("## Student model\n\nSource: `" + MODEL_SOURCE + "`\n\n"
                + "| Field | Type |\n| --- | --- |\n");
        for (JavaSourceExtractor.Component component : student.components()) {
            out.append("| ").append(cell(component.name())).append(" | ")
                    .append(cell(component.type())).append(" |\n");
        }
        return out.toString();
    }

    private static String requestBlock(JavaSourceExtractor.Result api) {
        StringBuilder out = new StringBuilder("## Create request form parameters\n\nSource: `"
                + API_SOURCE + "`\n\n| Parameter |\n| --- |\n");
        for (String name : api.requestParameters()) {
            out.append("| ").append(cell(name)).append(" |\n");
        }
        return out.toString();
    }

    private static String cell(String value) {
        return value.replace("\\", "\\\\").replace("|", "\\|").replace("\r", " ").replace("\n", " ");
    }
}
