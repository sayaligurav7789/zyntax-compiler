package minijava.web;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

class MiniJavaWebServerTest {

    private MiniJavaWebServer server;
    private HttpClient client;
    private String baseUrl;

    @BeforeEach
    void setUp() throws IOException {
        // Port 0 binds to any available ephemeral port
        server = new MiniJavaWebServer(0);
        server.start();
        baseUrl = "http://localhost:" + server.getPort();
        client = HttpClient.newHttpClient();
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    @DisplayName("GET / returns 200 OK and serves the HTML single-page visualizer")
    void testGetHtmlPage() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("ZYNTAX"));
        assertTrue(response.body().contains("Code Optimization Engine"));
    }

    @Test
    @DisplayName("GET /api/examples returns 200 OK and JSON array of example files")
    void testGetExamples() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/examples"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(response.body().startsWith("["));
        assertTrue(response.body().contains("01_prompt_example.minij"));
    }

    @Test
    @DisplayName("POST /api/compile successfully compiles, optimizes, and returns JSON report")
    void testPostCompile() throws Exception {
        String jsonPayload = """
                {
                  "source": "int a = 2 + 3;\\nint b = a * 4;\\nprint(b);",
                  "passFilter": "all"
                }
                """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/compile"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        String body = response.body();

        assertTrue(body.contains("\"success\":true"), "Response must indicate compilation success");
        assertTrue(body.contains("\"reductionPercentage\":80.0"), "Must report 80% instruction reduction");
        assertTrue(body.contains("\"output\":[\"20\"]"), "Must capture expected print output 20");
        assertTrue(body.contains("\"equivalent\":true"), "Must verify semantic equivalence");
    }

    @Test
    @DisplayName("POST /api/compile returns error response for invalid syntax")
    void testPostCompileSyntaxError() throws Exception {
        String jsonPayload = """
                {
                  "source": "int a = 2 + ;",
                  "passFilter": "all"
                }
                """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/compile"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        String body = response.body();

        assertTrue(body.contains("\"success\":false"));
        assertTrue(body.contains("ParseException"));
    }
}
