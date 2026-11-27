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
                "<form><input id='name' required></form>"
                + "<section><h2>Registered Students</h2><table><thead><tr>"
                + "<th>Student Name</th><th>Email</th><th>Department</th><th>Phone</th>"
                + "</tr></thead><tbody></tbody></table></section>");
        Files.writeString(root.resolve("frontend/app.js"),
                "const validationRules = { name: { required: true } };");
        Files.writeString(root.resolve("docs/ui/student-ui.md"),
                "# UI\nmanual before\n<!-- docsync:begin ui-fields sources=frontend/index.html -->\n"
                + "old\n<!-- docsync:end ui-fields -->\nmanual between\n"
                + "<!-- docsync:begin ui-validation sources=frontend/app.js -->\n"
                + "old\n<!-- docsync:end ui-validation -->\n"
                + "<!-- docsync:begin ui-registered-students sources=frontend/index.html -->\n"
                + "old\n<!-- docsync:end ui-registered-students -->\nmanual after\n");
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
                + "<!-- docsync:end api-request -->\n"
                + "<!-- docsync:begin api-list-response sources=x -->\nold\n"
                + "<!-- docsync:end api-list-response -->\nmanual contract note\n");
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
                + "  private void attachRoutes() {\n"
                + "    server.createContext(\"/students\", exchange -> {\n"
                + "      switch (exchange.getRequestMethod()) {\n"
                + "        case \"GET\" -> listStudents(exchange);\n"
                + "        case \"POST\" -> createStudent(exchange);\n"
                + "      }\n"
                + "    });\n"
                + "  }\n"
                + "  private static void listStudents(HttpExchange exchange) throws IOException {\n"
                + "    sendJson(exchange, 200, studentsAsJson(SERVICE.getStudents()));\n"
                + "  }\n"
                + "  private static String studentsAsJson(List<Student> students) {\n"
                + "    StringBuilder json = new StringBuilder(\"[\");\n"
                + "    for (int index = 0; index < students.size(); index++) {\n"
                + "      if (index > 0) { json.append(','); }\n"
                + "      Student student = students.get(index);\n"
                + "      json.append('{');\n"
                + "      appendJsonField(json, \"id\", student.id()); json.append(',');\n"
                + "      appendJsonField(json, \"name\", student.name()); json.append(',');\n"
                + "      appendJsonField(json, \"email\", student.email()); json.append(',');\n"
                + "      appendJsonField(json, \"department\", student.department()); json.append(',');\n"
                + "      appendJsonField(json, \"phone\", student.phone()); json.append(',');\n"
                + "      appendJsonField(json, \"status\", student.status());\n"
                + "      json.append('}');\n"
                + "    }\n"
                + "    return json.append(']').toString();\n"
                + "  }\n"
                + "  private static void appendJsonField(StringBuilder json, String name, String value) {\n"
                + "    appendJsonString(json, name); json.append(':');\n"
                + "    if (value == null) { json.append(\"null\"); }\n"
                + "    else { appendJsonString(json, value); }\n"
                + "  }\n"
                + "  private static void appendJsonString(StringBuilder json, String value) {\n"
                + "    json.append('\"');\n"
                + "    for (int index = 0; index < value.length(); index++) {\n"
                + "      char character = value.charAt(index);\n"
                + "      switch (character) {\n"
                + "        case '\"' -> json.append(\"\\\\\\\"\");\n"
                + "        case '\\\\' -> json.append(\"\\\\\\\\\");\n"
                + "        case '\\b' -> json.append(\"\\\\b\");\n"
                + "        case '\\f' -> json.append(\"\\\\f\");\n"
                + "        case '\\n' -> json.append(\"\\\\n\");\n"
                + "        case '\\r' -> json.append(\"\\\\r\");\n"
                + "        case '\\t' -> json.append(\"\\\\t\");\n"
                + "        default -> {\n"
                + "          if (character < 0x20) {\n"
                + "            json.append(\"\\\\u00\");\n"
                + "            json.append(Character.forDigit((character >> 4) & 0xF, 16));\n"
                + "            json.append(Character.forDigit(character & 0xF, 16));\n"
                + "          } else {\n"
                + "            json.append(character);\n"
                + "          }\n"
                + "        }\n"
                + "      }\n"
                + "    }\n"
                + "    json.append('\"');\n"
                + "  }\n"
                + "  private static void sendJson(HttpExchange exchange, int status, String body) throws IOException {\n"
                + "    byte[] payload = body.getBytes(StandardCharsets.UTF_8);\n"
                + "    exchange.getResponseHeaders().add(\"Content-Type\", \"application/json\");\n"
                + "    exchange.sendResponseHeaders(status, payload.length);\n"
                + "    try (OutputStream os = exchange.getResponseBody()) { os.write(payload); }\n"
                + "  }\n"
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
        assertTrue(doc(BackendApiSync.CONTRACT_DOC)
                .contains("Successful `GET /students` response"));
        assertTrue(doc(BackendApiSync.CONTRACT_DOC).contains("`appendJsonField` and `appendJsonString`"));
        assertTrue(doc(BackendApiSync.CONTRACT_DOC).contains("Empty collection serialization: `[]`"));
        assertTrue(doc(BackendApiSync.CONTRACT_DOC).contains("id, name, email, department, phone, status"));
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
    void syncAndVerifyReportsDescribeExpandedScopeAndLiveBehaviorLimits() throws Exception {
        DocSyncCli.sync(root.toString());
        String syncReport = doc("docs/reports/sync-report.md");
        assertTrue(syncReport.contains("registered-students table headers"));
        assertTrue(syncReport.contains("source-derived `GET /students` response contract"));
        assertTrue(syncReport.contains("Route declarations are separate from HTTP dispatch."));
        assertTrue(syncReport.contains("Model fields are not proof of wire-format behavior."));
        assertTrue(syncReport.contains("The extracted GET response contract describes source structure only; "
                + "live HTTP behavior is not verified."));

        DocSyncCli.verify(root.toString());
        String verifyReport = doc("docs/reports/verify-report.md");
        assertTrue(verifyReport.contains("registered-students table headers"));
        assertTrue(verifyReport.contains("source-derived `GET /students` response"));
        assertTrue(verifyReport.contains("Route declarations do not prove HTTP dispatch."));
        assertTrue(verifyReport.contains("Model fields are not proof of wire-format behavior."));
        assertTrue(verifyReport.contains("The source-derived GET response describes source structure only; "
                + "live HTTP behavior is not verified."));
    }

    @Test
    void registeredStudentsHeadersAreExtractedAndSourceChangesBecomeStale() throws Exception {
        DocSyncCli.sync(root.toString());
        Path ui = root.resolve("docs/ui/student-ui.md");
        assertTrue(doc("docs/ui/student-ui.md")
                .contains("Table header labels (source order): Student Name, Email, Department, Phone"));
        Path html = root.resolve("frontend/index.html");
        Files.writeString(html, Files.readString(html).replace("<th>Email</th>", "<th>Student Email</th>"));
        assertTrue(assertThrows(IOException.class, () -> DocSyncCli.verify(root.toString()))
                .getMessage().contains("STALE"));
        String beforeSync = Files.readString(ui);
        DocSyncCli.sync(root.toString());
        String afterSync = Files.readString(ui);
        assertTrue(afterSync.contains("Student Name, Student Email, Department, Phone"));
        assertTrue(afterSync.contains("manual before\n"));
        assertTrue(afterSync.contains("manual between\n"));
        assertTrue(afterSync.endsWith("manual after\n"));
        DocSyncCli.sync(root.toString());
        assertEquals(afterSync, Files.readString(ui));
        assertFalse(beforeSync.equals(afterSync));
    }

    @Test
    void registeredStudentsExtractionRejectsMissingDuplicateAndMalformedSection() throws Exception {
        Path html = root.resolve("frontend/index.html");
        String valid = Files.readString(html);
        for (String invalid : new String[] {
                "<form><input id='name'></form>",
                valid + valid.substring(valid.indexOf("<section>")),
                valid.replace("<thead><tr><th>Student Name</th><th>Email</th><th>Department</th><th>Phone</th>"
                        + "</tr></thead>", "<tbody></tbody>")
        }) {
            Files.writeString(html, invalid);
            String before = doc("docs/ui/student-ui.md");
            assertThrows(IOException.class, () -> DocSyncCli.sync(root.toString()));
            assertEquals(before, doc("docs/ui/student-ui.md"));
        }
    }

    @Test
    void listStudentsResponseSourceChangesUpdateContractAndUnsupportedChainFailsClosed() throws Exception {
        DocSyncCli.sync(root.toString());
        Path apiPath = root.resolve(BackendApiSync.API_SOURCE);
        String original = Files.readString(apiPath);

        Files.writeString(apiPath, original.replace("sendJson(exchange, 200, studentsAsJson",
                "sendJson(exchange, 202, studentsAsJson"));
        assertTrue(assertThrows(IOException.class, () -> DocSyncCli.verify(root.toString()))
                .getMessage().contains("STALE"));
        DocSyncCli.sync(root.toString());
        assertTrue(doc(BackendApiSync.CONTRACT_DOC).contains("Status: `202`"));

        String changedMediaType = Files.readString(apiPath)
                .replace("\"Content-Type\", \"application/json\"", "\"Content-Type\", \"application/problem+json\"");
        Files.writeString(apiPath, changedMediaType);
        assertTrue(assertThrows(IOException.class, () -> DocSyncCli.verify(root.toString()))
                .getMessage().contains("STALE"));
        DocSyncCli.sync(root.toString());
        assertTrue(doc(BackendApiSync.CONTRACT_DOC).contains("application/problem+json"));

        String changedField = Files.readString(apiPath)
                .replace("appendJsonField(json, \"phone\", student.phone())",
                        "appendJsonField(json, \"telephone\", student.phone())");
        Files.writeString(apiPath, changedField);
        assertThrows(IOException.class, () -> DocSyncCli.verify(root.toString()));
        DocSyncCli.sync(root.toString());
        assertTrue(doc(BackendApiSync.CONTRACT_DOC).contains("id, name, email, department, telephone, status"));

        String before = doc(BackendApiSync.CONTRACT_DOC);
        Files.writeString(apiPath, Files.readString(apiPath).replace(
                "studentsAsJson(SERVICE.getStudents())", "SERVICE.getStudents().toString()"));
        assertThrows(IOException.class, () -> DocSyncCli.sync(root.toString()));
        assertEquals(before, doc(BackendApiSync.CONTRACT_DOC));

        Files.writeString(apiPath, Files.readString(apiPath).replace(
                "case \"GET\" -> listStudents(exchange);", "case \"GET\" -> createStudent(exchange);"));
        assertThrows(IOException.class, () -> DocSyncCli.sync(root.toString()));
        assertEquals(before, doc(BackendApiSync.CONTRACT_DOC));
    }

    @Test
    void rawStringSerializationFailsVerificationAndSyncWithoutChangingDocs() throws Exception {
        DocSyncCli.sync(root.toString());
        Path apiPath = root.resolve(BackendApiSync.API_SOURCE);
        String originalSource = Files.readString(apiPath);
        String beforeContract = doc(BackendApiSync.CONTRACT_DOC);
        assertTrue(beforeContract.contains("Top-level JSON value: array"));
        assertTrue(beforeContract.contains("Serialized object fields (source order):"));
        assertTrue(beforeContract.contains("manual contract note"));

        String rawSerializer = originalSource.replace(
                "    json.append('\"');\n    for (int index = 0; index < value.length(); index++) {",
                "    json.append(value);\n    for (int index = 0; index < value.length(); index++) {");
        assertFalse(originalSource.equals(rawSerializer));
        Files.writeString(apiPath, rawSerializer);

        IOException stale = assertThrows(IOException.class, () -> DocSyncCli.verify(root.toString()));
        assertTrue(stale.getMessage().contains("Unsupported GET /students response source"));
        assertThrows(IOException.class, () -> DocSyncCli.sync(root.toString()));
        assertEquals(beforeContract, doc(BackendApiSync.CONTRACT_DOC));
        assertTrue(doc("docs/reports/sync-report.md").contains("Failed"));
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
                + "<input id='studentPhone' type='tel' required></form>"
                + "<section><h2>Registered Students</h2><table><thead><tr>"
                + "<th>Student Name</th><th>Email</th><th>Department</th><th>Phone</th>"
                + "</tr></thead><tbody></tbody></table></section>");
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

        Files.writeString(html, "<form><input id='name' required></form>"
                + "<section><h2>Registered Students</h2><table><thead><tr>"
                + "<th>Student Name</th><th>Email</th><th>Department</th><th>Phone</th>"
                + "</tr></thead><tbody></tbody></table></section>");
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
        Path contract = root.resolve(BackendApiSync.CONTRACT_DOC);
        String text = Files.readString(doc).replace("\n", "\r\n");
        String contractText = Files.readString(contract).replace("\n", "\r\n");
        Files.writeString(doc, text);
        Files.writeString(contract, contractText);
        DocSyncCli.sync(root.toString());
        String once = Files.readString(doc);
        String contractOnce = Files.readString(contract);
        assertTrue(once.startsWith("# UI\r\nmanual before\r\n"));
        assertTrue(once.contains("\r\nmanual between\r\n"));
        assertTrue(once.endsWith("\r\nmanual after\r\n"));
        assertFalse(once.replace("\r\n", "").contains("\n"));
        assertTrue(contractOnce.startsWith("# Contract\r\n"));
        assertTrue(contractOnce.endsWith("manual contract note\r\n"));
        assertFalse(contractOnce.replace("\r\n", "").contains("\n"));
        DocSyncCli.sync(root.toString());
        assertEquals(once, Files.readString(doc));
        assertEquals(contractOnce, Files.readString(contract));
    }
}
