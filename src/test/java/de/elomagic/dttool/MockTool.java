package de.elomagic.dttool;

import de.elomagic.dttool.configuration.Configuration;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.function.Executable;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

public class MockTool {

    public static void mockServer(int port, Executable executable) throws Throwable {

        String apiKey = UUID.randomUUID().toString();
        String projects = IOUtils.resourceToString("projects.json", StandardCharsets.UTF_8, DTrackClientTest.class.getClassLoader());
        String bom = IOUtils.resourceToString("bom-example.json", StandardCharsets.UTF_8, DTrackClientTest.class.getClassLoader());

        Configuration.INSTANCE.setApiKey(apiKey);
        Configuration.INSTANCE.setBaseUrl("http://localhost:%s".formatted(port));

        configureFor("localhost", port);

        // When download projects
        stubFor(get(urlPathEqualTo("/api/v1/project"))
                .withQueryParam("page", equalTo("1"))
                .withHeader("X-Api-Key", equalTo(apiKey))
                .willReturn(okJson(projects)));
        stubFor(get(urlPathEqualTo("/api/v1/project"))
                .withQueryParam("page", equalTo("2"))
                .withHeader("X-Api-Key", equalTo(apiKey))
                .willReturn(okJson("[]")));

        // When delete project
        stubFor(delete(urlPathMatching("/api/v1/project/.*"))
                .withHeader("X-Api-Key", equalTo(apiKey))
                .willReturn(noContent()));

        // When download projects
        stubFor(get(urlPathMatching("/api/v1/bom/cyclonedx/project/.*"))
                .withQueryParam("download", equalTo("false"))
                .withHeader("X-Api-Key", equalTo(apiKey))
                .willReturn(okJson(bom)));

        executable.execute();

    }

}
