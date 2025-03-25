package de.elomagic.dttool;

import de.elomagic.dttool.configuration.Configuration;
import de.elomagic.dttool.dt.DTrackClient;
import de.elomagic.dttool.dt.model.Project;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.chrono.ChronoZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.junit.jupiter.api.Assertions.*;

class DTrackClientTest extends AbstractMockedServer {

    @Test
    void testGetProjects() throws Exception {
        String apiKey = UUID.randomUUID().toString();
        String ps = IOUtils.resourceToString("projects.json", StandardCharsets.UTF_8, DTrackClientTest.class.getClassLoader());

        configureFor("localhost", getPort());

        // When download SPXID licenses
        stubFor(get(urlPathMatching("/api/v1/project"))
                .withHeader("X-Api-Key", equalTo(apiKey))
                .willReturn(okJson(ps)));

        Configuration.INSTANCE.setApiKey(apiKey);
        Configuration.INSTANCE.setBaseUrl("http://localhost:%s".formatted(getPort()));

        // Execute the text
        DTrackClient dtClient = new DTrackClient();
        List<Project> projects = dtClient.fetchProjects(10, 1);
        assertEquals(7, projects.size());

        assertEquals(1517598568L, Optional.ofNullable(projects.getFirst().getLastBomImport()).map(ChronoZonedDateTime::toEpochSecond).orElse(0L));

    }

}