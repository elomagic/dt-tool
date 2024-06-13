package de.elomagic.dttool;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.jupiter.MockServerExtension;

import java.io.IOException;

@ExtendWith(MockServerExtension.class)
class AppTest {

    @Test
    void testHelp() {
        App.main(new String[] { "-help" });
    }

    @Test
    void testProjectCare(MockServerClient client) throws IOException {
        MockTool.mockServer(client);

        Configuration.INSTANCE.setVersionMatch("\\d+\\.\\d+\\.\\d+\\.\\d+-SNAPSHOT");
        Configuration.INSTANCE.setOlderThenDays(90);

        App.main(new String[] { "-pc" });

//        String content = Files.readString(Path.of("./junit.log"), StandardCharsets.UTF_8);
//        assertTrue(content.contains("Patching pkg:maven/org.glassfish.jersey.core/jersey-client@2.41"), "At least one licence are not patched");
//        assertFalse(content.contains("\"expression\""), "Some or all expressions not patched");
    }

}