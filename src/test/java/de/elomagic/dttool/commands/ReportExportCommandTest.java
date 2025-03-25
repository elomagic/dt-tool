package de.elomagic.dttool.commands;

import de.elomagic.dttool.AbstractMockedServer;
import de.elomagic.dttool.App;
import de.elomagic.dttool.ConsolePrinter;
import de.elomagic.dttool.MockTool;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

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
            int exitCode = app.execute(new String[]{"report", "-nbd=4365", "--file=./target/test/report.csv"});

            assertEquals(0, exitCode);
            assertTrue(Files.exists(Path.of("./target/test/report.csv")));
            assertTrue(Files.size(Path.of("./target/test/report.csv")) > 200);
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
