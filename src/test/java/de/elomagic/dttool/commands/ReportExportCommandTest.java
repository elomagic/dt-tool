package de.elomagic.dttool.commands;

import de.elomagic.dttool.AbstractMockedServer;
import de.elomagic.dttool.App;
import de.elomagic.dttool.ConsolePrinter;
import de.elomagic.dttool.MockTool;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportExportCommandTest extends AbstractMockedServer {

    @BeforeAll
    static void beforeAll() {
        ConsolePrinter.INSTANCE.setDebug(false);
        ConsolePrinter.INSTANCE.setVerbose(false);
    }

    @Test
    void testWriteCSV() throws Throwable {
        MockTool.mockServer(getPort(), () -> {
            App app = new App();
            int exitCode = app.execute(new String[]{"report", "-nbd=4365", "--file=./target/test/report.csv", "-ds=,"});

            Path file = Paths.get("./target/test/report.csv");
            assertEquals(0, exitCode);
            assertThat(Files.readString(file, StandardCharsets.UTF_8)).containsPattern("projectName;flooredBomDate;reportDate;averageInheritedRiskScore.*");
            assertThat(Files.readString(file, StandardCharsets.UTF_8)).containsPattern(".*TestLatestVersion1;2018-01;.*;32,.0.*");
            assertThat(Files.readString(file, StandardCharsets.UTF_8)).containsPattern(".*TestProject;2018-02;.*;230,0.*");
        });
    }

    @Test
    void testWriteJson() throws Throwable {
        MockTool.mockServer(getPort(), () -> {
            App app = new App();
            int exitCode = app.execute(new String[]{"report", "-nbd=4365", "--file=./target/test/report.json", "--format=JSON"});

            assertEquals(0, exitCode);
            assertTrue(Files.exists(Path.of("./target/test/report.json")));
            assertTrue(Files.size(Path.of("./target/test/report.json")) > 200);
        });
    }

}
