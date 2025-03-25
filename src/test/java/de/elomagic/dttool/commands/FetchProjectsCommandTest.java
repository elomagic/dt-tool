package de.elomagic.dttool.commands;

import de.elomagic.dttool.AbstractMockedServer;
import de.elomagic.dttool.App;
import de.elomagic.dttool.ConsolePrinter;
import de.elomagic.dttool.MockTool;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FetchProjectsCommandTest extends AbstractMockedServer {

    private static final Logger LOGGER = LogManager.getLogger(FetchProjectsCommandTest.class);
    private static final Path LOG_FILE = Path.of("./junit.log");

    @BeforeAll
    static void beforeAll() {
        ConsolePrinter.INSTANCE.setDebug(false);
        ConsolePrinter.INSTANCE.setVerbose(false);
    }

    @Test
    void testLatestVersion() throws Throwable {
        MockTool.mockServer(getPort(), () -> {
            LOGGER.info("ABC1");
            App app = new App();
            int exitCode = app.execute(new String[]{"fetch-projects", "--projectFilter=ProjectDoesNotExist", "-nad=30", "-mc=1", "--format=VERSION"});
            LOGGER.info("ABC1");

            assertEquals(0, exitCode);
            assertEquals("", getText("ABC1"));
        });
    }

    @Test
    void testLatestVersion3() throws Throwable {
        MockTool.mockServer(getPort(), () -> {
            LOGGER.info("ABC3");
            App app = new App();
            int exitCode = app.execute(new String[] { "fetch-projects", "--projectFilter=TestLatestVersion1", "-nad=30","--format=VERSION"});
            LOGGER.info("ABC3");

            assertEquals(0, exitCode);
            assertThat(getText("ABC3")).contains("1.0.0.1");
        });
    }

    @Test
    void testLatestVersion4() throws Throwable {
        MockTool.mockServer(getPort(), () -> {
            LOGGER.info("ABC4");
            App app = new App();
            int exitCode = app.execute(new String[] { "fetch-projects", "--projectFilter=TestLatestVersion1", "-nad=30", "--format=UUID" });
            LOGGER.info("ABC4");

            assertEquals(0, exitCode);
            assertThat(getText("ABC4")).contains("85b0f240-b405-4d61-a10a-42f54b6ad59e");
        });
    }

    @Test
    void testLatestVersion5() throws Throwable {
        MockTool.mockServer(getPort(), () -> {
            LOGGER.info("ABC5");
            App app = new App();
            int exitCode = app.execute(new String[] { "fetch-projects", "--projectFilter=TestLatestVersion1", "-nad=30", "--format=JSON" });
            LOGGER.info("ABC5");

            assertEquals(0, exitCode);
            assertThat(getText("ABC5")).contains("{  \"uuid\" : \"85b0f240-b405-4d61-a10a-42f54b6ad59e\",  \"name\" : \"TestLatestVersion1\",  \"version\" : \"1.0.0.1\",  \"purl\" : null,  \"lastBomImport\" : \"1517198568503\",  \"metrics\" : {    \"critical\" : 0,    \"high\" : 1,    \"medium\" : 3,    \"low\" : 0,    \"unassigned\" : 0,    \"vulnerabilities\" : 4,    \"vulnerableComponents\" : 2,    \"components\" : 133,    \"suppressed\" : 0,    \"findingsTotal\" : 4,    \"findingsAudited\" : 0,    \"findingsUnaudited\" : 4,    \"inheritedRiskScore\" : 50.0,    \"policyViolationsFail\" : 0,    \"policyViolationsWarn\" : 0,    \"policyViolationsInfo\" : 0,    \"policyViolationsTotal\" : 0,    \"policyViolationsAudited\" : 0,    \"policyViolationsUnaudited\" : 0,    \"policyViolationsSecurityTotal\" : 0,    \"policyViolationsSecurityAudited\" : 0,    \"policyViolationsSecurityUnaudited\" : 0,    \"policyViolationsLicenseTotal\" : 0,    \"policyViolationsLicenseAudited\" : 0,    \"policyViolationsLicenseUnaudited\" : 0,    \"policyViolationsOperationalTotal\" : 0,    \"policyViolationsOperationalAudited\" : 0,    \"policyViolationsOperationalUnaudited\" : 0,    \"firstOccurrence\" : 1741600680649,    \"lastOccurrence\" : 1742911404985  }}");
        });
    }

    private String getText(String s) throws IOException {

        String logText = readLog();

        Pattern pattern = Pattern.compile(".*%s(?<V>.*)%s.*".formatted(s, s));

        Matcher matcher = pattern.matcher(logText);

        return matcher.find() ? matcher.group("V") : null;

    }

    private String readLog() throws IOException {

        return Files
                .readString(LOG_FILE, StandardCharsets.UTF_8)
                .replace("\r", "")
                .replace("\n", "");

    }

}