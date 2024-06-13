package de.elomagic.dttool;

import shaded_package.org.apache.commons.io.IOUtils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.jupiter.MockServerExtension;

import java.nio.charset.StandardCharsets;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@ExtendWith(MockServerExtension.class)
class ProjectCareTest {

    @Test
    void testCare(MockServerClient client) throws Exception {
        MockTool.mockServer(client);

        Configuration.INSTANCE.setVersionMatch("\\d+\\.\\d+\\.\\d+\\.\\d+-SNAPSHOT");
        Configuration.INSTANCE.setOlderThenDays(90);

        ProjectCare pc = new ProjectCare();
        pc.care();
    }
}