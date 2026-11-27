package org.example.api;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StudentApiServerTest {
    @Test
    void listsStudentsAsJsonForEmptyAndPopulatedRepository() throws IOException, InterruptedException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        StudentApiServer.attachRoutes(server);
        server.start();
        try {
            HttpClient client = HttpClient.newHttpClient();
            URI studentsUri = URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/students");

            HttpResponse<String> emptyResponse = client.send(
                    HttpRequest.newBuilder(studentsUri).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            assertEquals(200, emptyResponse.statusCode());
            assertTrue(emptyResponse.headers().firstValue("Content-Type").orElse("")
                    .startsWith("application/json"));
            assertEquals("[]", emptyResponse.body());

            HttpResponse<String> invalidCreate = client.send(
                    HttpRequest.newBuilder(studentsUri)
                            .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                            .POST(HttpRequest.BodyPublishers.ofString("name=&email=invalid&department=Math"))
                            .build(),
                    HttpResponse.BodyHandlers.ofString());
            assertEquals(400, invalidCreate.statusCode());

            String name = "Ada \"A\" \\\nLovelace";
            String createBody = formField("name", name)
                    + "&" + formField("email", "ada@example.com")
                    + "&" + formField("department", "Computer Science")
                    + "&" + formField("phone", "");
            HttpResponse<String> createResponse = client.send(
                    HttpRequest.newBuilder(studentsUri)
                            .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                            .POST(HttpRequest.BodyPublishers.ofString(createBody))
                            .build(),
                    HttpResponse.BodyHandlers.ofString());
            assertEquals(201, createResponse.statusCode());
            String createdId = createResponse.body().substring(
                    "Student[id=".length(), createResponse.body().indexOf(','));

            HttpResponse<String> populatedResponse = client.send(
                    HttpRequest.newBuilder(studentsUri).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            assertEquals(200, populatedResponse.statusCode());
            assertTrue(populatedResponse.headers().firstValue("Content-Type").orElse("")
                    .startsWith("application/json"));
            String escapedName = name
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n");
            assertEquals("[{\"id\":\"" + createdId
                    + "\",\"name\":\"" + escapedName
                    + "\",\"email\":\"ada@example.com\",\"department\":\"Computer Science\","
                    + "\"phone\":\"\",\"status\":\"active\"}]", populatedResponse.body());
        } finally {
            server.stop(0);
        }
    }

    private static String formField(String name, String value) {
        return URLEncoder.encode(name, StandardCharsets.UTF_8)
                + "=" + URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
