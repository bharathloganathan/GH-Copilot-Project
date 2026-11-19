package org.example.docsync;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocSyncCliTest {
    @TempDir
    Path root;

    @BeforeEach
    void setUp() throws IOException {
        Files.createDirectories(root.resolve("frontend"));
        Files.createDirectories(root.resolve("docs/ui"));
        Files.createDirectories(root.resolve("docs/backend"));
        Files.writeString(root.resolve("frontend/index.html"),
                "<form><input id='name' required></form>");
        Files.writeString(root.resolve("frontend/app.js"),
                "const validationRules = { name: { required: true } };");
        Files.writeString(root.resolve("docs/ui/student-ui.md"),
                "# UI\nmanual before\n<!-- docsync:begin ui-fields sources=frontend/index.html -->\n"
                + "old\n<!-- docsync:end ui-fields -->\nmanual between\n"
                + "<!-- docsync:begin ui-validation sources=frontend/app.js -->\n"
                + "old\n<!-- docsync:end ui-validation -->\nmanual after\n");
        Files.writeString(root.resolve("docs/backend/manual.md"), "backend unchanged");

        Files.createDirectories(root.resolve("docs/api"));
        Files.createDirectories(root.resolve("src/main/java/org/example/service"));
        Files.createDirectories(root.resolve("src/main/java/org/example/api"));
        Files.createDirectories(root.resolve("src/main/java/org/example/model"));
        Files.writeString(root.resolve(BackendApiSync.SERVICE_SOURCE), service(""));
        Files.writeString(root.resolve(BackendApiSync.API_SOURCE), api(routeGet("/students")
                + ",\n" + routePost("/students"), "phone"));
        Files.writeString(root.resolve(BackendApiSync.MODEL_SOURCE),
                "package org.example.model;\npublic record Student(String id, String name) {}\n");
        Files.writeString(root.resolve(BackendApiSync.SERVICE_DOC),
                "# Service\nmanual\n<!-- docsync:begin backend-service sources=x -->\nold\n"
                + "<!-- docsync:end backend-service -->\nmanual after\n");
        Files.writeString(root.resolve(BackendApiSync.ROUTES_DOC),
                "# Routes\n<!-- docsync:begin api-routes sources=x -->\nold\n"
                + "<!-- docsync:end api-routes -->\nmanual routes note\n");
        Files.writeString(root.resolve(BackendApiSync.CONTRACT_DOC),
                "# Contract\n<!-- docsync:begin api-contract sources=x -->\nold\n"
                + "<!-- docsync:end api-contract -->\n"
                + "<!-- docsync:begin api-request sources=x -->\nold\n"
                + "<!-- docsync:end api-request -->\nmanual contract note\n");
    }

    private static String service(String extra) {
        return "package org.example.service;\npublic class StudentService {\n"
                + "  public String find(String id) { return id; }\n" + extra + "}\n";
    }

    private static String routeGet(String path) {
        return "new Route(\"GET\", \"" + path + "\", \"List\", \"none\", \"Student[]\", null)";
    }

    private static String routePost(String path) {
        return "new Route(\"POST\", \"" + path + "\", \"Create\", \"StudentRequest\", \"Student\", null)";
    }

    private static String api(String routes, String param) {
        return "package org.example.api;\nimport java.util.*;\npublic class StudentApiServer {\n"
                + "  public static List<Route> routes() { return List.of(" + routes + "); }\n"
                + "  private static void createStudent(Object exchange) {\n"
                + "    Map<String, String> params = null;\n"
                + "    params.getOrDefault(\"name\", \"\");\n"
                + (param.isEmpty() ? "" : "    params.getOrDefault(\"" + param + "\", \"\");\n")
                + "  }\n  public record Route(String method, String path, String summary,"
                + " String requestType, String responseType, Object handler) {}\n}\n";
    }

    private String doc(String relative) throws IOException {
        return Files.readString(root.resolve(relative));
    }

    @Test
    void backendAndApiDocsFollowSourceChanges() throws Exception {
        DocSyncCli.sync(root.toString());
        assertTrue(doc(BackendApiSync.SERVICE_DOC).contains("find(String id)"));
        assertTrue(doc(BackendApiSync.ROUTES_DOC).contains("| GET | /students | List |"));
        assertTrue(doc(BackendApiSync.CONTRACT_DOC).contains("| name | String |"));
        assertTrue(doc(BackendApiSync.CONTRACT_DOC).contains("| phone |"));
        DocSyncCli.verify(root.toString());

        // new backend method
        Files.writeString(root.resolve(BackendApiSync.SERVICE_SOURCE),
                service("  public boolean remove(String id) { return true; }\n"));
        assertTrue(assertThrows(IOException.class, () -> DocSyncCli.verify(root.toString()))
                .getMessage().contains("student-service.md"));
        DocSyncCli.sync(root.toString());
        assertTrue(doc(BackendApiSync.SERVICE_DOC).contains("boolean remove(String id)"));

        // business logic change alters the recorded source digest
        String before = doc(BackendApiSync.SERVICE_DOC);
        Files.writeString(root.resolve(BackendApiSync.SERVICE_SOURCE),
                service("  public boolean remove(String id) { return false; }\n"));
        DocSyncCli.sync(root.toString());
        assertFalse(before.equals(doc(BackendApiSync.SERVICE_DOC)));

        // changed endpoint path and HTTP method
        Files.writeString(root.resolve(BackendApiSync.API_SOURCE), api(
                routeGet("/learners") + ",\n"
                        + routePost("/students").replace("\"POST\"", "\"PUT\""), "phone"));
        DocSyncCli.sync(root.toString());
        String routes = doc(BackendApiSync.ROUTES_DOC);
        assertTrue(routes.contains("| GET | /learners |"));
        assertTrue(routes.contains("| PUT | /students |"));
        assertFalse(routes.contains("| POST | /students |"));

        // deleted endpoint is reported and removed from the generated block
        Files.writeString(root.resolve(BackendApiSync.API_SOURCE), api(routeGet("/learners"), "phone"));
        assertTrue(assertThrows(IOException.class, () -> DocSyncCli.verify(root.toString()))
                .getMessage().contains("endpoints.md"));
        DocSyncCli.sync(root.toString());
        assertFalse(doc(BackendApiSync.ROUTES_DOC).contains("PUT"));
        assertTrue(Files.readString(root.resolve("docs/reports/sync-report.md"))
                .contains("Removed/changed declaration row"));

        // request and model field changes
        Files.writeString(root.resolve(BackendApiSync.API_SOURCE), api(routeGet("/learners"), "nickname"));
        Files.writeString(root.resolve(BackendApiSync.MODEL_SOURCE),
                "package org.example.model;\npublic record Student(String id, String nickname) {}\n");
        DocSyncCli.sync(root.toString());
        assertTrue(doc(BackendApiSync.CONTRACT_DOC).contains("| nickname |"));
        assertFalse(doc(BackendApiSync.CONTRACT_DOC).contains("| phone |"));
        assertTrue(doc(BackendApiSync.CONTRACT_DOC).contains("manual contract note"));
        assertTrue(doc(BackendApiSync.ROUTES_DOC).contains("manual routes note"));
        DocSyncCli.verify(root.toString());
    }

    @Test
    void dynamicRouteMetadataFailsWithoutOverwriting() throws Exception {
        Files.writeString(root.resolve(BackendApiSync.API_SOURCE), api(
                "new Route(method(), \"/x\", \"s\", \"none\", \"S\", null)", "phone"));
        String before = doc(BackendApiSync.ROUTES_DOC);
        assertThrows(IOException.class, () -> DocSyncCli.sync(root.toString()));
        assertEquals(before, doc(BackendApiSync.ROUTES_DOC));
    }

    @Test
    void addAndRemovePhoneWithoutChangingGenerator() throws Exception {
        DocSyncCli.sync(root.toString());
        Path html = root.resolve("frontend/index.html");
        Files.writeString(html, "<form><input id='name' required>"
                + "<input id='studentPhone' type='tel' required></form>");
        assertTrue(assertThrows(IOException.class, () -> DocSyncCli.verify(root.toString()))
                .getMessage().contains("STALE"));
        DocSyncCli.sync(root.toString());
        String doc = Files.readString(root.resolve("docs/ui/student-ui.md"));
        assertTrue(doc.contains("| studentPhone | tel | Yes |"));
        assertTrue(doc.contains("manual before\n"));
        assertTrue(doc.contains("manual between\n"));
        assertTrue(doc.endsWith("manual after\n"));
        assertEquals("backend unchanged", Files.readString(root.resolve("docs/backend/manual.md")));
        DocSyncCli.verify(root.toString());

        Files.writeString(html, "<form><input id='name' required></form>");
        assertTrue(assertThrows(IOException.class, () -> DocSyncCli.verify(root.toString()))
                .getMessage().contains("STALE"));
        DocSyncCli.sync(root.toString());
        assertFalse(Files.readString(root.resolve("docs/ui/student-ui.md")).contains("studentPhone"));
    }

    @Test
    void changesToValidationAreReflected() throws Exception {
        Files.writeString(root.resolve("frontend/app.js"),
                "const validationRules = { name: { pattern: /^\\d{7,15}$/, minLength: 4 } };");
        DocSyncCli.sync(root.toString());
        String doc = Files.readString(root.resolve("docs/ui/student-ui.md"));
        assertTrue(doc.contains("minLength: 4"));
        assertTrue(doc.contains("\\d{7,15}"));
    }

    @Test
    void missingOrDuplicateMarkersCauseFailureWithoutOverwriting() throws Exception {
        Path doc = root.resolve("docs/ui/student-ui.md");
        String text = "manual only";
        Files.writeString(doc, text);
        assertThrows(IOException.class, () -> DocSyncCli.sync(root.toString()));
        assertEquals(text, Files.readString(doc));
        assertTrue(Files.readString(root.resolve("docs/reports/sync-report.md")).contains("Failed"));
    }

    @Test
    void rejectsUnsupportedValidationAndPreservesDocument() throws Exception {
        Files.writeString(root.resolve("frontend/app.js"),
                "const validationRules = { name: { message: `template` } };");
        Path doc = root.resolve("docs/ui/student-ui.md");
        String before = Files.readString(doc);
        assertThrows(IOException.class, () -> DocSyncCli.sync(root.toString()));
        assertEquals(before, Files.readString(doc));
    }

    @Test
    void preservesCrLfAndManualContentAndIsIdempotent() throws Exception {
        Path doc = root.resolve("docs/ui/student-ui.md");
        String text = Files.readString(doc).replace("\n", "\r\n");
        Files.writeString(doc, text);
        DocSyncCli.sync(root.toString());
        String once = Files.readString(doc);
        assertTrue(once.startsWith("# UI\r\nmanual before\r\n"));
        assertTrue(once.contains("\r\nmanual between\r\n"));
        assertTrue(once.endsWith("\r\nmanual after\r\n"));
        assertFalse(once.replace("\r\n", "").contains("\n"));
        DocSyncCli.sync(root.toString());
        assertEquals(once, Files.readString(doc));
    }
}
