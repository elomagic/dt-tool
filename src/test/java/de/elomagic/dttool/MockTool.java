package de.elomagic.dttool;

import shaded_package.org.apache.commons.io.IOUtils;

import org.mockserver.client.MockServerClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class MockTool {

    public static void mockServer(MockServerClient client) throws IOException {

        int port = client.getPort();
        String apiKey = "abcdefghijklmnopqrstuvwxyz1234567890";
        String licenses = IOUtils.resourceToString("projects.json", StandardCharsets.UTF_8, DTrackClientTest.class.getClassLoader());

        // When download projects
        client.when(
                        request()
                                .withMethod("GET")
                                .withQueryStringParameter("page", "1")
                                .withPath("/api/v1/project")
                                .withHeader("X-Api-Key", apiKey)
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(licenses)
                );
        client.when(
                        request()
                                .withMethod("GET")
                                .withQueryStringParameter("page", "2")
                                .withPath("/api/v1/project")
                                .withHeader("X-Api-Key", apiKey)
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody("[]")
                );

        // When delete project
        client.when(
                        request()
                                .withMethod("DELETE")
                                .withPath("/api/v1/project/01d558ae-5075-4cbb-94ea-73ce6ae23999")
                                .withHeader("X-Api-Key", apiKey)
                )
                .respond(
                        response()
                                .withStatusCode(204)
                );

        Configuration.INSTANCE.setApiKey(apiKey);
        Configuration.INSTANCE.setBaseUrl("http://localhost:%s".formatted(port));
    }

}
