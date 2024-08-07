package de.elomagic.dttool;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.jupiter.MockServerExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockServerExtension.class)
class GetLatestTest {

    @Test
    void testLatestVersion(MockServerClient client) throws IOException {
        MockTool.mockServer(client);

        GetLatest lv = new GetLatest();

        assertTrue(lv.getLatest("ProjectDoesNotExist").isEmpty());

        Configuration.INSTANCE.setReturnProperty(ProjectResult.VERSION);
        assertEquals("1.0.0.1", lv.getLatest("TestLatestVersion1").orElseThrow());

        Configuration.INSTANCE.setReturnProperty(ProjectResult.UUID);
        assertEquals("85b0f240-b405-4d61-a10a-42f54b6ad59e", lv.getLatest("TestLatestVersion1").orElseThrow());

        Configuration.INSTANCE.setReturnProperty(ProjectResult.JSON);
        assertEquals("{\"uuid\":\"85b0f240-b405-4d61-a10a-42f54b6ad59e\",\"name\":\"TestLatestVersion1\",\"version\":\"1.0.0.1\",\"lastBomImport\":\"1517198568503\"}", lv.getLatest("TestLatestVersion1").orElseThrow());
    }

}