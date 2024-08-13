package de.elomagic.dttool;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

class BomCareTest extends AbstractMockedServer {

    @Test
    void care() throws IOException {
        // ConsolePrinter.INSTANCE.setDebug(true);
        String projects = IOUtils.resourceToString("projects.json", StandardCharsets.UTF_8, DTrackClientTest.class.getClassLoader());
        String sbom1 = IOUtils.resourceToString("test-sbom-invalid-license-ids.json", StandardCharsets.UTF_8, DTrackClientTest.class.getClassLoader());
        String sbom2 = IOUtils.resourceToString("test-sbom-02.json", StandardCharsets.UTF_8, DTrackClientTest.class.getClassLoader());
        String apiKey = UUID.randomUUID().toString();

        try (MockServerClient client = new MockServerClient("localhost", getPort())) {

            // When download SPXID licenses
            client.when(
                            request()
                                    .withMethod("GET")
                                    .withPath("/api/v1/project")
                                    .withQueryStringParameter("page", "1")
                                    .withHeader("X-Api-Key", apiKey)
                    )
                    .respond(
                            response()
                                    .withStatusCode(200)
                                    .withBody(projects)
                    );
            client.when(
                            request()
                                    .withMethod("GET")
                                    .withPath("/api/v1/project")
                                    .withQueryStringParameter("page", "2")
                                    .withHeader("X-Api-Key", apiKey)
                    )
                    .respond(
                            response()
                                    .withStatusCode(200)
                                    .withBody("[]")
                    );
            client.when(
                            request()
                                    .withMethod("GET")
                                    .withPath("/api/v1/bom/cyclonedx/project/01d558ae-5075-4cbb-94ea-73ce6ae23532")
                                    .withHeader("X-Api-Key", apiKey)
                    )
                    .respond(
                            response()
                                    .withStatusCode(200)
                                    .withBody(sbom1)
                    );
            client.when(
                            request()
                                    .withMethod("GET")
                                    .withPath("/api/v1/bom/cyclonedx/project/.*")//01d558ae-5075-4cbb-94ea-73ce6ae23532")
                                    .withHeader("X-Api-Key", apiKey)
                    )
                    .respond(
                            response()
                                    .withStatusCode(200)
                                    .withBody(sbom2)
                    );

            Configuration.INSTANCE.setApiKey(apiKey);
            Configuration.INSTANCE.setBaseUrl("http://localhost:%s".formatted(getPort()));

            // Execute the text
            BomCare care = new BomCare();
            care.care();

        }
    }
}