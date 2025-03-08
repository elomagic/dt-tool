package de.elomagic.dttool;

import de.elomagic.dttool.configuration.Configuration;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.function.Executable;
import org.mockserver.client.MockServerClient;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class MockTool {

    public static void mockServer(int port, Executable executable) throws Throwable {

        String apiKey = UUID.randomUUID().toString();
        String licenses = IOUtils.resourceToString("projects.json", StandardCharsets.UTF_8, DTrackClientTest.class.getClassLoader());

        try (MockServerClient client = new MockServerClient("localhost", port)) {
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
                                    //.withPath("/api/v1/project/01d558ae-5075-4cbb-94ea-73ce6ae23999")
                                    .withHeader("X-Api-Key", apiKey)
                    )
                    .respond(
                            response()
                                    .withStatusCode(204)
                    );

            Configuration.INSTANCE.setApiKey(apiKey);
            Configuration.INSTANCE.setBaseUrl("http://localhost:%s".formatted(port));

            executable.execute();

        }
    }

}
