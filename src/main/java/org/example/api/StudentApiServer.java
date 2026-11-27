package org.example.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.example.model.Student;
import org.example.service.StudentService;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class StudentApiServer {
    private static final StudentService SERVICE = new StudentService();

    public static List<Route> routes() {
        return List.of(
            new Route("GET", "/students", "List all students", "none", "Student[]", StudentApiServer::listStudents),
            new Route("POST", "/students", "Create a student", "StudentRequest", "Student", StudentApiServer::createStudent),
            new Route("GET", "/students/{id}", "Fetch a student by id", "none", "Student", StudentApiServer::getStudentById),
            new Route("DELETE", "/students/{id}", "Delete a student", "none", "DeleteResponse", StudentApiServer::deleteStudent)
        );
    }

    public static void attachRoutes(HttpServer server) {
        server.createContext("/", exchange -> {
            byte[] body = java.nio.file.Files.readAllBytes(java.nio.file.Path.of("frontend/index.html"));
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });

        server.createContext("/style.css", exchange -> {
            byte[] body = java.nio.file.Files.readAllBytes(java.nio.file.Path.of("frontend/style.css"));
            exchange.getResponseHeaders().set("Content-Type", "text/css; charset=UTF-8");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });

        server.createContext("/app.js", exchange -> {
            byte[] body = java.nio.file.Files.readAllBytes(java.nio.file.Path.of("frontend/app.js"));
            exchange.getResponseHeaders().set("Content-Type", "application/javascript; charset=UTF-8");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });

        server.createContext("/students", exchange -> {
            switch (exchange.getRequestMethod()) {
                case "GET" -> listStudents(exchange);
                case "POST" -> createStudent(exchange);
                default -> sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }
        });
        server.createContext("/students/", exchange -> {
            switch (exchange.getRequestMethod()) {
                case "GET" -> getStudentById(exchange);
                case "DELETE" -> deleteStudent(exchange);
                default -> sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }
        });
    }

    public record Route(String method, String path, String summary, String requestType, String responseType, HttpHandler handler) {
    }

    private static void listStudents(HttpExchange exchange) throws IOException {
        sendJson(exchange, 200, studentsAsJson(SERVICE.getStudents()));
    }

    private static String studentsAsJson(List<Student> students) {
        StringBuilder json = new StringBuilder("[");
        for (int index = 0; index < students.size(); index++) {
            if (index > 0) {
                json.append(',');
            }
            Student student = students.get(index);
            json.append('{');
            appendJsonField(json, "id", student.id());
            json.append(',');
            appendJsonField(json, "name", student.name());
            json.append(',');
            appendJsonField(json, "email", student.email());
            json.append(',');
            appendJsonField(json, "department", student.department());
            json.append(',');
            appendJsonField(json, "phone", student.phone());
            json.append(',');
            appendJsonField(json, "status", student.status());
            json.append('}');
        }
        return json.append(']').toString();
    }

    private static void appendJsonField(StringBuilder json, String name, String value) {
        appendJsonString(json, name);
        json.append(':');
        if (value == null) {
            json.append("null");
        } else {
            appendJsonString(json, value);
        }
    }

    private static void appendJsonString(StringBuilder json, String value) {
        json.append('"');
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            switch (character) {
                case '"' -> json.append("\\\"");
                case '\\' -> json.append("\\\\");
                case '\b' -> json.append("\\b");
                case '\f' -> json.append("\\f");
                case '\n' -> json.append("\\n");
                case '\r' -> json.append("\\r");
                case '\t' -> json.append("\\t");
                default -> {
                    if (character < 0x20) {
                        json.append("\\u00");
                        json.append(Character.forDigit((character >> 4) & 0xF, 16));
                        json.append(Character.forDigit(character & 0xF, 16));
                    } else {
                        json.append(character);
                    }
                }
            }
        }
        json.append('"');
    }

    private static void createStudent(HttpExchange exchange) throws IOException {
        Map<String, String> params = readForm(exchange);
        Student student;
        try {
            student = SERVICE.createStudent(
                params.getOrDefault("name", ""),
                params.getOrDefault("email", ""),
                params.getOrDefault("department", ""),
                params.getOrDefault("phone", "")
            );
        } catch (IllegalArgumentException e) {
            sendJson(exchange, 400, "{\"error\":\"Invalid student input\"}");
            return;
        }
        sendJson(exchange, 201, student.toString());
    }

    private static void getStudentById(HttpExchange exchange) throws IOException {
        String id = exchange.getRequestURI().getPath().substring("/students/".length());
        Optional<Student> student = SERVICE.getStudent(id);
        if (student.isPresent()) {
            sendJson(exchange, 200, student.get().toString());
        } else {
            sendJson(exchange, 404, "{\"error\":\"Student not found\"}");
        }
    }

    private static void deleteStudent(HttpExchange exchange) throws IOException {
        String id = exchange.getRequestURI().getPath().substring("/students/".length());
        boolean deleted = SERVICE.deleteStudent(id);
        sendJson(exchange, deleted ? 200 : 404, "{\"deleted\":" + deleted + "}");
    }

    private static Map<String, String> readForm(HttpExchange exchange) throws IOException {
        String rawBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        if (rawBody.isBlank()) {
            return Map.of();
        }
        return parseForm(rawBody);
    }

    private static Map<String, String> parseForm(String rawBody) {
        java.util.Map<String, String> values = new java.util.HashMap<>();
        for (String pair : rawBody.split("&")) {
            String[] item = pair.split("=", 2);
            if (item.length == 2) {
                values.put(item[0], java.net.URLDecoder.decode(item[1], StandardCharsets.UTF_8));
            }
        }
        return values;
    }

    private static void sendJson(HttpExchange exchange, int status, String body) throws IOException {
        byte[] payload = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, payload.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(payload);
        }
    }
}