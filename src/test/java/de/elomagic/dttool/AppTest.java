package de.elomagic.dttool;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.jupiter.MockServerExtension;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockServerExtension.class)
class AppTest {

    @Test
    void testHelp() throws IOException {
        App.main(new String[] { "-help" });

        String content = readLog();

        assertThat(content)
                .doesNotContain("v{project.version}")
                .contains("Dependency Track Tool v");
    }

    @Test
    void testProjectCare(MockServerClient client) throws IOException {
        MockTool.mockServer(client);

        Configuration.INSTANCE.setVersionMatch(Configuration.DEFAULT_PROJECT_VERSION_MATCH);
        Configuration.INSTANCE.setOlderThenDays(90);

        App.main(new String[] { "-pc", "-b", "-v" });

        assertThat(readLog()).contains("TestLatestVersion1\t 1.0.0.1-SNAPSHOT\t Created 2018-01-29T05:02:48.503+01:00[Europe/Berlin]");
    }

    @Test
    void testLatestVersion(MockServerClient client) throws IOException {
        MockTool.mockServer(client);

        App.main(new String[] { "-l", "-pn", "TestLatestVersion1", "-rp", "VERSION" });

        assertThat(readLog()).contains("1.0.0.1\n");
    }

    private String readLog() throws IOException {
        return Files
                .readString(Path.of("./junit.log"), StandardCharsets.UTF_8)
                .replace("\r\n", "\n");
    }

}