package de.elomagic.dttool;

import de.elomagic.dttool.configuration.Configuration;

import org.junit.jupiter.api.Test;

class ProjectCareTest extends AbstractMockedServer {

    @Test
    void testCare() throws Throwable {
        MockTool.mockServer(getPort(), () -> {
            Configuration.INSTANCE.setVersionMatch(Configuration.DEFAULT_PROJECT_VERSION_MATCH);
            Configuration.INSTANCE.setOlderThenDays(30);
            Configuration.INSTANCE.setBatchMode(true);

            ProjectCare pc = new ProjectCare();
            pc.care();
        });
    }

}