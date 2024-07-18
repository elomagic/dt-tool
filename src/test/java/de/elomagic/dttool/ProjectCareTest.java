package de.elomagic.dttool;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.jupiter.MockServerExtension;

@ExtendWith(MockServerExtension.class)
class ProjectCareTest {

    @Test
    void testCare(MockServerClient client) throws Exception {
        MockTool.mockServer(client);

        Configuration.INSTANCE.setVersionMatch("\\d+\\.\\d+\\.\\d+\\.\\d+-SNAPSHOT");
        Configuration.INSTANCE.setOlderThenDays(30);
        Configuration.INSTANCE.setBatchMode(true);

        ProjectCare pc = new ProjectCare();
        pc.care();
    }

}