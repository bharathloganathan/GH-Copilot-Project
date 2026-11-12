package org.example;

import com.sun.net.httpserver.HttpServer;
import org.example.api.StudentApiServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        StudentApiServer.attachRoutes(server);
        server.start();
        System.out.println("Student API server started on http://localhost:8000");
    }
}
