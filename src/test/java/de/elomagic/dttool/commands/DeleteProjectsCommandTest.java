package de.elomagic.dttool.commands;

import de.elomagic.dttool.AbstractMockedServer;
import de.elomagic.dttool.App;
import de.elomagic.dttool.MockTool;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DeleteProjectsCommandTest extends AbstractMockedServer {

    @Test
    void testDeleteProject() throws Throwable {
        MockTool.mockServer(getPort(), () -> {
            App app = new App();
            int exitCode = app.execute(new String[] { "delete-projects", "--notAfterDays=999", "-b" });

            assertEquals(0, exitCode);
        });
    }

}
