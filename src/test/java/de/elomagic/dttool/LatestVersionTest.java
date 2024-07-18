package de.elomagic.dttool;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.jupiter.MockServerExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockServerExtension.class)
class LatestVersionTest {

    @Test
    void testLatestVersion(MockServerClient client) throws IOException {
        MockTool.mockServer(client);

        LatestVersion lv = new LatestVersion();

        assertTrue(lv.getLatestVersion("ProjectDoesNotExist").isEmpty());
        assertEquals("1.0.0.1", lv.getLatestVersion("TestLatestVersion1").get());
    }

}