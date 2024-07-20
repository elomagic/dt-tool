package de.elomagic.dttool;

import shaded_package.org.apache.commons.io.IOUtils;

import de.elomagic.dttool.model.Project;
import de.elomagic.dttool.model.Violation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.jupiter.MockServerExtension;

import java.nio.charset.StandardCharsets;
import java.time.chrono.ChronoZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@ExtendWith(MockServerExtension.class)
class DTrackClientTest {

    @Test
    void testGetProjects(MockServerClient client) throws Exception {
        int port = client.getPort();
        String apiKey = "abcdefghijklmnopqrstuvwxyz1234567890";
        String licenses = IOUtils.resourceToString("projects.json", StandardCharsets.UTF_8, DTrackClientTest.class.getClassLoader());

        // When download SPXID licenses
        client.when(
                        request()
                                .withMethod("GET")
                                .withPath("/api/v1/project")
                                .withHeader("X-Api-Key", apiKey)
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(licenses)
                );

        Configuration.INSTANCE.setApiKey(apiKey);
        Configuration.INSTANCE.setBaseUrl("http://localhost:%s".formatted(port));

        // Execute the text
        DTrackClient dtClient = new DTrackClient();
        List<Project> projects = dtClient.fetchProjects(10, 1);
        assertEquals(7, projects.size());

        assertEquals(1517598568L, Optional.ofNullable(projects.getFirst().getLastBomImport()).map(ChronoZonedDateTime::toEpochSecond).orElse(0L));
    }

}