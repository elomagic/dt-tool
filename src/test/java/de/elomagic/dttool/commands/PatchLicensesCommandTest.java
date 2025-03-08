package de.elomagic.dttool.commands;

import de.elomagic.dttool.AbstractMockedServer;
import de.elomagic.dttool.App;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.RegexBody;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

class PatchLicensesCommandTest extends AbstractMockedServer {

    @Test
    void testPatch() throws IOException {
        String projects = IOUtils.resourceToString("projects-future.json5", StandardCharsets.UTF_8, PatchLicensesCommandTest.class.getClassLoader());
        String comps = IOUtils.resourceToString("components-01.json5", StandardCharsets.UTF_8, PatchLicensesCommandTest.class.getClassLoader());
        String comp = IOUtils.resourceToString("component-34012ff4-a94d-44d2-bdc4-4aa63577d96f.json5", StandardCharsets.UTF_8, PatchLicensesCommandTest.class.getClassLoader());
        String license = IOUtils.resourceToString("license-apache-2.0.json5", StandardCharsets.UTF_8, PatchLicensesCommandTest.class.getClassLoader());
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
                                    .withPath("/api/v1/component/project/01d558ae-5075-4cbb-94ea-73ce6ae23532")
                                    .withQueryStringParameter("page", "1")
                                    .withHeader("X-Api-Key", apiKey)
                    )
                    .respond(
                            response()
                                    .withStatusCode(200)
                                    .withBody(comps)
                    );
            client.when(
                            request()
                                    .withMethod("GET")
                                    .withPath("/api/v1/component/project/01d558ae-5075-4cbb-94ea-73ce6ae23532")
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
                                    .withPath("/api/v1/component/project/.*")//01d558ae-5075-4cbb-94ea-73ce6ae23532")
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
                                    .withPath("/api/v1/component/34012ff4-a94d-44d2-bdc4-4aa63577d96f")
                                    .withHeader("X-Api-Key", apiKey)
                    )
                    .respond(
                            response()
                                    .withStatusCode(200)
                                    .withBody(comp)
                    );

            client.when(
                            request()
                                    .withMethod("POST")
                                    .withPath("/api/v1/component")
                                    .withHeader("X-Api-Key", apiKey)
                                    .withBody(new RegexBody(".*resolvedLicense.*"))
                                    .withBody(new RegexBody(".*Apache-2.0.*"))
                    )
                    .respond(
                            response()
                                    .withStatusCode(200)
                                    .withBody(comp)
                    );

            client.when(
                            request()
                                    .withMethod("GET")
                                    .withPath("/api/v1/license/Apache-2.0")
                                    .withHeader("X-Api-Key", apiKey)
                    )
                    .respond(
                            response()
                                    .withStatusCode(200)
                                    .withBody(license)
                    );

            assertThat(comp)
                    .doesNotContainPattern("resolvedLicense")
                    .doesNotContainPattern("Apache-2.0");

            App app = new App();
            int exitCode = app.execute(new String[]{"patch-licenses", "--apiKey=" + apiKey, "--baseUrl=http://localhost:%s".formatted(getPort()), "--batchMode"});

            assertEquals(0, exitCode);

            // TODO Assert successful patch
        }
    }
}