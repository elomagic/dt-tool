package de.elomagic.dttool.commands;

import de.elomagic.dttool.AbstractMockedServer;
import de.elomagic.dttool.App;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CheckLicensesCommandTest extends AbstractMockedServer {

    @Test
    void testPatch() throws IOException {
        String projects = IOUtils.resourceToString("projects-future.json5", StandardCharsets.UTF_8, CheckLicensesCommandTest.class.getClassLoader());
        String comps = IOUtils.resourceToString("components-01.json5", StandardCharsets.UTF_8, CheckLicensesCommandTest.class.getClassLoader());
        String comp = IOUtils.resourceToString("component-34012ff4-a94d-44d2-bdc4-4aa63577d96f.json5", StandardCharsets.UTF_8, CheckLicensesCommandTest.class.getClassLoader());
        String license = IOUtils.resourceToString("license-apache-2.0.json5", StandardCharsets.UTF_8, CheckLicensesCommandTest.class.getClassLoader());
        String apiKey = UUID.randomUUID().toString();

        configureFor("localhost", getPort());

        // When download SPXID licenses
        stubFor(get(urlPathEqualTo("/api/v1/project"))
                .withQueryParam("page", equalTo("1"))
                .withHeader("X-Api-Key", equalTo(apiKey))
                .willReturn(okJson(projects)));
        stubFor(get(urlPathEqualTo("/api/v1/project"))
                .withQueryParam("page", equalTo("2"))
                .withHeader("X-Api-Key", equalTo(apiKey))
                .willReturn(okJson("[]")));

        stubFor(get(urlPathEqualTo("/api/v1/component/project/01d558ae-5075-4cbb-94ea-73ce6ae23532"))
                .withQueryParam("page", equalTo("1"))
                .withHeader("X-Api-Key", equalTo(apiKey))
                .willReturn(okJson(comps)));

        stubFor(get(urlPathMatching("/api/v1/component/project/.*"))
                .withHeader("X-Api-Key", equalTo(apiKey))
                .willReturn(okJson("[]")));

        stubFor(get(urlPathEqualTo("/api/v1/component/34012ff4-a94d-44d2-bdc4-4aa63577d96f"))
                .withHeader("X-Api-Key", equalTo(apiKey))
                .willReturn(okJson(comp)));

        stubFor(post(urlPathEqualTo("/api/v1/component"))
                .withHeader("X-Api-Key", equalTo(apiKey))
                .withRequestBody(containing("resolvedLicense"))
                .withRequestBody(containing("Apache-2.0"))
                .willReturn(okJson(comp)));

        stubFor(get(urlPathEqualTo("/api/v1/license/Apache-2.0"))
                .withHeader("X-Api-Key", equalTo(apiKey))
                .willReturn(okJson(license)));

        assertThat(comp)
                .doesNotContainPattern("resolvedLicense")
                .doesNotContainPattern("Apache-2.0");

        App app = new App();
        int exitCode = app.execute(new String[]{"check-licenses", "--apiKey=" + apiKey, "--baseUrl=http://localhost:%s".formatted(getPort()), "--batchMode"});

        assertEquals(0, exitCode);

        // TODO Assert successful patch
    }
}